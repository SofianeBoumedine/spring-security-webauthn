/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sharplab.springframework.security.webauthn.server;

import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import net.sharplab.springframework.security.webauthn.challenge.ChallengeRepository;
import net.sharplab.springframework.security.webauthn.options.OptionsProvider;
import net.sharplab.springframework.security.webauthn.util.ServletUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * {@inheritDoc}
 */
public class ServerPropertyProviderImpl implements ServerPropertyProvider {

    //~ Instance fields
    // ================================================================================================
    private OptionsProvider optionsProvider;
    private ChallengeRepository challengeRepository;

    public ServerPropertyProviderImpl(OptionsProvider optionsProvider, ChallengeRepository challengeRepository) {
        this.optionsProvider = optionsProvider;
        this.challengeRepository = challengeRepository;
    }

    /**
     * {@inheritDoc}
     */
    public ServerProperty provide(HttpServletRequest request) {

        Origin origin = ServletUtil.getOrigin(request);
        String effectiveRpId = optionsProvider.getEffectiveRpId(request);
        Challenge challenge = challengeRepository.loadOrGenerateChallenge(request);

        return new ServerProperty(origin, effectiveRpId, challenge, null); // tokenBinding is not supported by Servlet API as of 4.0
    }


}
