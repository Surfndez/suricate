/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.suricate.monitoring.service.scheduler;

import io.suricate.monitoring.model.dto.nashorn.NashornRequest;
import io.suricate.monitoring.model.dto.nashorn.NashornResponse;
import io.suricate.monitoring.model.dto.nashorn.WidgetVariableResponse;
import io.suricate.monitoring.model.entity.project.Project;
import io.suricate.monitoring.model.entity.project.ProjectWidget;
import io.suricate.monitoring.model.enums.WidgetState;
import io.suricate.monitoring.service.api.ProjectWidgetService;
import io.suricate.monitoring.service.api.WidgetService;
import io.suricate.monitoring.service.nashorn.NashornService;
import io.suricate.monitoring.service.nashorn.task.NashornResultAsyncTask;
import io.suricate.monitoring.service.nashorn.task.NashornWidgetExecuteAsyncTask;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class NashornWidgetScheduler implements Schedulable {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NashornWidgetScheduler.class.getName());

    /**
     * The number of executor
     */
    private static final int EXECUTOR_POOL_SIZE = 60;
    /**
     * Start widget process immediately
     */
    private static final long SMALL_DELAY = 2L;

    /**
     * Widget start delay inclusive
     */
    private static final int START_DELAY_INCLUSIVE = 30;

    /**
     * Widget end delay exclusive
     */
    private static final int END_DELAY_EXCLUSIVE = 120;

    /**
     * thread executor service
     */
    private ScheduledThreadPoolExecutor scheduledExecutorService;
    private ScheduledThreadPoolExecutor scheduledExecutorServiceFuture;

    /**
     * The project widget service
     */
    private ProjectWidgetService projectWidgetService;
    /**
     * The nashorn service
     */
    private NashornService nashornService;
    /**
     * The Spring boot application context
     */
    private ApplicationContext ctx;
    /**
     * The string encryptor
     */
    private StringEncryptor stringEncryptor;
    /**
     * Map containing all current scheduled jobs
     */
    private Map<Long, Pair<WeakReference<ScheduledFuture<NashornResponse>>, WeakReference<ScheduledFuture<Void>>>> jobs = new ConcurrentHashMap<>();

    /**
     * Constructor
     *
     * @param applicationContext         The application context to inject
     * @param projectWidgetService       The project widget service to inject
     * @param nashornService             The nashorn service to inject
     * @param stringEncryptor            The string encryptor to inject
     */
    @Autowired
    public NashornWidgetScheduler(final ApplicationContext applicationContext,
                                  @Lazy final ProjectWidgetService projectWidgetService,
                                  final NashornService nashornService,
                                  @Qualifier("jasyptStringEncryptor") final StringEncryptor stringEncryptor) {
        this.ctx = applicationContext;
        this.projectWidgetService = projectWidgetService;
        this.nashornService = nashornService;
        this.stringEncryptor = stringEncryptor;
    }

    /**
     * Method used to cancel a scheduled future for an widget instance
     *
     * @param projectWidgetId project widget Id
     * @param weakReference   weakReference containing the ScheduledFuture or null
     */
    private static void cancel(Long projectWidgetId, WeakReference<? extends ScheduledFuture> weakReference) {
        if (weakReference != null) {
            ScheduledFuture scheduledFuture = weakReference.get();
            if (scheduledFuture != null && (!scheduledFuture.isDone() || !scheduledFuture.isCancelled())) {
                LOGGER.debug("Cancel task for widget instance {} ({})", projectWidgetId, scheduledFuture);
                scheduledFuture.cancel(true);
            }
        }
    }

    /**
     * Method used to init scheduler
     */
    @Transactional
    public void initScheduler() {
        LOGGER.info("Init widget scheduler");

        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
        scheduledExecutorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(EXECUTOR_POOL_SIZE);
        scheduledExecutorService.setRemoveOnCancelPolicy(true);

        if (scheduledExecutorServiceFuture != null) {
            scheduledExecutorServiceFuture.shutdownNow();
        }
        scheduledExecutorServiceFuture = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(EXECUTOR_POOL_SIZE);
        scheduledExecutorServiceFuture.setRemoveOnCancelPolicy(true);
        // clear jobs
        jobs.clear();

        projectWidgetService.resetProjectWidgetsState();
    }

    /**
     * Schedule a list of nashorn request
     *
     * @param nashornRequests The list of nashorn requests to schedule
     * @param start           If the scheduling should start now
     * @param init            If it's an initialisation of the scheduling
     */
    public void scheduleList(final List<NashornRequest> nashornRequests, boolean start, boolean init) {
        try {
            nashornRequests.forEach(nashornRequest -> schedule(nashornRequest, start, init));

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Method used to schedule widget update
     *
     * @param nashornRequest nashorn request
     * @param start          force widget update to start now
     * @param init           force widget to update randomly between START_DELAY_INCLUSIVE and END_DELAY_EXCLUSIVE
     */
    public void schedule(final NashornRequest nashornRequest, boolean start, boolean init) {
        if (nashornRequest == null) {
            return;
        }
        // Get the beans inside schedule
        ProjectWidgetService projectWidgetServiceInjected = ctx.getBean(ProjectWidgetService.class);
        WidgetService widgetService = ctx.getBean(WidgetService.class);

        if (!nashornService.isNashornRequestExecutable(nashornRequest)) {
            projectWidgetServiceInjected.updateState(WidgetState.STOPPED, nashornRequest.getProjectWidgetId(), new Date());
            return;
        }

        // Update the status if necessary
        if (WidgetState.STOPPED == nashornRequest.getWidgetState()) {
            LOGGER.debug("Scheduled widget instance:{}", nashornRequest.getProjectWidgetId());
            projectWidgetServiceInjected.updateState(WidgetState.RUNNING, nashornRequest.getProjectWidgetId(), new Date());
        }

        Long delay = nashornRequest.getDelay();
        if (start) {
            delay = RandomUtils.nextLong(START_DELAY_INCLUSIVE, END_DELAY_EXCLUSIVE);
        } else if (init) {
            delay = SMALL_DELAY;
        }

        ProjectWidget projectWidget = projectWidgetServiceInjected.getOne(nashornRequest.getProjectWidgetId()).orElse(new ProjectWidget());
        List<WidgetVariableResponse> widgetVariableResponses = widgetService.getWidgetVariables(projectWidget.getWidget());

        // Create scheduled future task
        ScheduledFuture<NashornResponse> future = scheduledExecutorService.schedule(new NashornWidgetExecuteAsyncTask(nashornRequest, stringEncryptor, widgetVariableResponses), delay, TimeUnit.SECONDS);

        // Create result future task
        NashornResultAsyncTask nashornResultAsyncTask = ctx.getBean(NashornResultAsyncTask.class, future, nashornRequest, this);
        ScheduledFuture<Void> futureResult = scheduledExecutorServiceFuture.schedule(nashornResultAsyncTask, delay, TimeUnit.SECONDS);

        // Update job
        jobs.put(nashornRequest.getProjectWidgetId(),
            new ImmutablePair<>(
                new WeakReference<>(future),
                new WeakReference<>(futureResult)
            ));

    }

    /**
     * Method used to cancelWidgetInstance the existing scheduled widget instance and launch a new instance
     *
     * @param nashornRequest the nashorn request to execute
     */
    public void cancelAndSchedule(NashornRequest nashornRequest) {
        cancelWidgetInstance(nashornRequest.getProjectWidgetId());
        schedule(nashornRequest, false, true);
    }

    /**
     * Method is used for cancelling the projectWidgets hold by a project
     *
     * @param project The project
     */
    public void cancelProjectScheduling(final Project project) {
        project
            .getWidgets()
            .forEach(projectWidget -> cancelWidgetInstance(projectWidget.getId()));
    }

    /**
     * Method used to cancelWidgetInstance the existing scheduled widget instance
     *
     * @param projectWidgetId the widget instance id
     */
    public void cancelWidgetInstance(Long projectWidgetId) {
        Pair<WeakReference<ScheduledFuture<NashornResponse>>, WeakReference<ScheduledFuture<Void>>> pair = jobs.get(projectWidgetId);
        if (pair != null) {
            cancel(projectWidgetId, pair.getLeft());
            cancel(projectWidgetId, pair.getRight());
        }
        projectWidgetService.updateState(WidgetState.STOPPED, projectWidgetId);
    }
}
