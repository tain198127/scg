package org.danebrown.protocol;

import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.Nullable;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

/**
 * Created by danebrown on 2021/4/8
 * mail: tain198127@163.com
 *
 * @author danebrown
 */

public final class MutateServerWebExchange extends DefaultServerWebExchange {
    private MutateServerWebExchange(MutatedServerHttpRequest request, WebSessionManager sessionManager) {
        super(request, new MutateServerHttpResponse(), sessionManager,
                ServerCodecConfigurer.create(), new AcceptHeaderLocaleContextResolver());
    }

    public static MutateServerWebExchange from(MutatedServerHttpRequest request) {
        return builder(request).build();
    }

//    public static MutateServerWebExchange from(BaseBuilder<?> requestBuilder) {
//        return builder(requestBuilder).build();
//    }

    public static MutateServerWebExchange.Builder builder(MutatedServerHttpRequest request) {
        return new MutateServerWebExchange.Builder(request);
    }

//    public static MutateServerWebExchange.Builder builder(BaseBuilder<?> requestBuilder) {
//        return new MutateServerWebExchange.Builder(requestBuilder.build());
//    }

    public MutateServerHttpResponse getResponse() {
        return (MutateServerHttpResponse) super.getResponse();
    }

    public static class Builder {
        private final MutatedServerHttpRequest request;
        @Nullable
        private WebSessionManager sessionManager;

        public Builder(MutatedServerHttpRequest request) {
            this.request = request;
        }

        public MutateServerWebExchange.Builder session(WebSession session) {
            this.sessionManager = (exchange) -> {
                return Mono.just(session);
            };
            return this;
        }

        public MutateServerWebExchange.Builder sessionManager(WebSessionManager sessionManager) {
            this.sessionManager = sessionManager;
            return this;
        }

        public MutateServerWebExchange build() {
            return new MutateServerWebExchange(this.request, (WebSessionManager) (this.sessionManager != null ? this.sessionManager : new DefaultWebSessionManager()));
        }
    }
}

