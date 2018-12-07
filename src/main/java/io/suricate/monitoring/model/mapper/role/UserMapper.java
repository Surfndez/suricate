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

package io.suricate.monitoring.model.mapper.role;

import io.suricate.monitoring.model.dto.api.user.UserRequestDto;
import io.suricate.monitoring.model.dto.api.user.UserResponseDto;
import io.suricate.monitoring.model.entity.user.User;
import io.suricate.monitoring.model.enums.AuthenticationMethod;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interface that manage the generation DTO/Model objects for User class
 */
@Component
@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /* ************************* TO DTO ********************************************** */

    /* ******************************************************* */
    /*                  Simple Mapping                         */
    /* ******************************************************* */

    /**
     * Tranform a User into a UserResponseDto
     *
     * @param user The user to transform
     * @return The related user DTO
     */
    @Named("toUserDtoDefault")
    @Mapping(target = "fullname", expression = "java(String.format(\"%s %s\", user.getFirstname(), user.getLastname()))")
    public abstract UserResponseDto toUserDtoDefault(User user);

    /* ******************************************************* */
    /*                    List Mapping                         */
    /* ******************************************************* */

    /**
     * Tranform a list of Users into a list of UserResponseDto
     *
     * @param users The list of user to transform
     * @return The related users DTO
     */
    @Named("toUserDtosDefault")
    @IterableMapping(qualifiedByName = "toUserDtoDefault")
    public abstract List<UserResponseDto> toUserDtosDefault(List<User> users);

    /* ************************* TO MODEL **************************************** */

    /* ******************************************************* */
    /*                  Simple Mapping                         */
    /* ******************************************************* */

    /**
     * Tranform a UserRequestDto into a User for creation
     *
     * @param userRequestDto       The userRequestDto to transform
     * @param authenticationMethod The authentication method of the user
     * @return The related user
     */
    @Named("toNewUser")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authenticationMethod", source = "authenticationMethod")
    @Mapping(target = "password", expression = "java( passwordEncoder.encode(userRequestDto.getPassword()) )")
    public abstract User toNewUser(UserRequestDto userRequestDto, AuthenticationMethod authenticationMethod);
}
