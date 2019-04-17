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

package io.suricate.monitoring.model.entity.project;


import io.suricate.monitoring.model.entity.AbstractAuditingEntity;
import io.suricate.monitoring.model.entity.Asset;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Project/dashboard entity
 */
@Entity
@Table(name = "PROJECT_SLIDE")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectSlide extends AbstractAuditingEntity<Long> {

    /**
     * The project id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The number of column
     */
    @Column
    private Integer maxColumn;

    /**
     * The height of the widgets
     */
    @Column
    private Integer widgetHeight;

    /**
     * The css style of the grid
     */
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String cssStyle;

    /**
     * The screenshot of the dashboard
     */
    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "screenshot_id")
    private Asset screenshot;

    /**
     * The related project of this slide
     */
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * The list of widgets related to it
     */
    @OneToMany(mappedBy = "projectSlide", cascade = CascadeType.REMOVE)
    @OrderBy("row ASC, col ASC")
    private List<ProjectWidget> widgets = new ArrayList<>();
}
