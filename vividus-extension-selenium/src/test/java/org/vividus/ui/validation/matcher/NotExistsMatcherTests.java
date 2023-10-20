/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.ui.validation.matcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class NotExistsMatcherTests
{
    private static final String DOES_NOT_EXISTS = "does not exist";
    private static final String EXISTS = "exists";
    private List<WebElement> webElements;

    @Mock
    private Description description;

    @InjectMocks
    private NotExistsMatcher notExistsMatcher;

    @Test
    void testMatchesSafely()
    {
        webElements = new ArrayList<>();
        assertTrue(notExistsMatcher.matchesSafely(webElements));
    }

    @Test
    void testDescribeTo()
    {
        notExistsMatcher.describeTo(description);
        verify(description).appendText(DOES_NOT_EXISTS);
    }

    @Test
    void testDescribeMismatchSafely()
    {
        notExistsMatcher.describeMismatchSafely(webElements, description);
        verify(description).appendText(EXISTS);
    }

    @Test
    void testNotExistsMatcher()
    {
        assertNotNull(NotExistsMatcher.notExists());
    }
}