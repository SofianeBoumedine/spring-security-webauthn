/*
 *    Copyright 2002-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.sharplab.springframework.security.webauthn.exception;

import org.junit.Test;

@SuppressWarnings("ThrowableNotThrown")
public class MissingChallengeExceptionTest {

    private RuntimeException cause = new RuntimeException();

    @Test
    public void test() {
        new MissingChallengeException("dummy", cause);
        new MissingChallengeException("dummy");
    }
}
