package org.danebrown.protocol;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.SslInfo;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by danebrown on 2021/4/8
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
public final class MutatedServerHttpRequest extends AbstractServerHttpRequest {
    private final HttpMethod httpMethod;
    private final MultiValueMap<String, HttpCookie> cookies;
    @Nullable
    private final InetSocketAddress remoteAddress;
    @Nullable
    private final SslInfo sslInfo;
    private final Flux<DataBuffer> body;

    private MutatedServerHttpRequest(HttpMethod httpMethod, URI uri, @Nullable String contextPath, HttpHeaders headers, MultiValueMap<String, HttpCookie> cookies, @Nullable InetSocketAddress remoteAddress, @Nullable SslInfo sslInfo, Publisher<? extends DataBuffer> body) {
        super(uri, contextPath, headers);
        this.httpMethod = httpMethod;
        this.cookies = cookies;
        this.remoteAddress = remoteAddress;
        this.sslInfo = sslInfo;
        this.body = Flux.from(body);
    }

    public HttpMethod getMethod() {
        return this.httpMethod;
    }

    public String getMethodValue() {
        return this.httpMethod.name();
    }

    @Nullable
    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Nullable
    protected SslInfo initSslInfo() {
        return this.sslInfo;
    }

    public Flux<DataBuffer> getBody() {
        return this.body;
    }

    protected MultiValueMap<String, HttpCookie> initCookies() {
        return this.cookies;
    }

    public <T> T getNativeRequest() {
        throw new IllegalStateException("This is a mock. No running server, no native request.");
    }

    public static MutatedServerHttpRequest.BodyBuilder method(HttpMethod method, URI url) {
        return new MutatedServerHttpRequest.DefaultBodyBuilder(method, url);
    }

    public static MutatedServerHttpRequest.BodyBuilder method(HttpMethod method, String urlTemplate, Object... vars) {
        URI url = UriComponentsBuilder.fromUriString(urlTemplate).buildAndExpand(vars).encode().toUri();
        return new MutatedServerHttpRequest.DefaultBodyBuilder(method, url);
    }

    public static MutatedServerHttpRequest.BaseBuilder<?> get(String urlTemplate, Object... uriVars) {
        return method(HttpMethod.GET, urlTemplate, uriVars);
    }

    public static MutatedServerHttpRequest.BaseBuilder<?> head(String urlTemplate, Object... uriVars) {
        return method(HttpMethod.HEAD, urlTemplate, uriVars);
    }

    public static MutatedServerHttpRequest.BodyBuilder post(String urlTemplate, Object... uriVars) {
        return method(HttpMethod.POST, urlTemplate, uriVars);
    }

    public static MutatedServerHttpRequest.BodyBuilder put(String urlTemplate, Object... uriVars) {
        return method(HttpMethod.PUT, urlTemplate, uriVars);
    }

    public static MutatedServerHttpRequest.BodyBuilder patch(String urlTemplate, Object... uriVars) {
        return method(HttpMethod.PATCH, urlTemplate, uriVars);
    }

    public static MutatedServerHttpRequest.BaseBuilder<?> delete(String urlTemplate, Object... uriVars) {
        return method(HttpMethod.DELETE, urlTemplate, uriVars);
    }

    public static MutatedServerHttpRequest.BaseBuilder<?> options(String urlTemplate, Object... uriVars) {
        return method(HttpMethod.OPTIONS, urlTemplate, uriVars);
    }

    private static class DefaultBodyBuilder implements MutatedServerHttpRequest.BodyBuilder {
        private static final DataBufferFactory BUFFER_FACTORY = new DefaultDataBufferFactory();
        private final HttpMethod method;
        private final URI url;
        @Nullable
        private String contextPath;
        private final UriComponentsBuilder queryParamsBuilder = UriComponentsBuilder.newInstance();
        private final HttpHeaders headers = new HttpHeaders();
        private final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap();
        @Nullable
        private InetSocketAddress remoteAddress;
        @Nullable
        private SslInfo sslInfo;

        public DefaultBodyBuilder(HttpMethod method, URI url) {
            this.method = method;
            this.url = url;
        }

        public MutatedServerHttpRequest.BodyBuilder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder queryParam(String name, Object... values) {
            this.queryParamsBuilder.queryParam(name, values);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder queryParams(MultiValueMap<String, String> params) {
            this.queryParamsBuilder.queryParams(params);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder remoteAddress(InetSocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public void sslInfo(SslInfo sslInfo) {
            this.sslInfo = sslInfo;
        }

        public MutatedServerHttpRequest.BodyBuilder cookie(HttpCookie... cookies) {
            Arrays.stream(cookies).forEach((cookie) -> {
                this.cookies.add(cookie.getName(), cookie);
            });
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder cookies(MultiValueMap<String, HttpCookie> cookies) {
            this.cookies.putAll(cookies);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder header(String headerName, String... headerValues) {
            String[] var3 = headerValues;
            int var4 = headerValues.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String headerValue = var3[var5];
                this.headers.add(headerName, headerValue);
            }

            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder headers(MultiValueMap<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder accept(MediaType... acceptableMediaTypes) {
            this.headers.setAccept(Arrays.asList(acceptableMediaTypes));
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder acceptCharset(Charset... acceptableCharsets) {
            this.headers.setAcceptCharset(Arrays.asList(acceptableCharsets));
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder acceptLanguageAsLocales(Locale... acceptableLocales) {
            this.headers.setAcceptLanguageAsLocales(Arrays.asList(acceptableLocales));
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder contentLength(long contentLength) {
            this.headers.setContentLength(contentLength);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder contentType(MediaType contentType) {
            this.headers.setContentType(contentType);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder ifModifiedSince(long ifModifiedSince) {
            this.headers.setIfModifiedSince(ifModifiedSince);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder ifUnmodifiedSince(long ifUnmodifiedSince) {
            this.headers.setIfUnmodifiedSince(ifUnmodifiedSince);
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder ifNoneMatch(String... ifNoneMatches) {
            this.headers.setIfNoneMatch(Arrays.asList(ifNoneMatches));
            return this;
        }

        public MutatedServerHttpRequest.BodyBuilder range(HttpRange... ranges) {
            this.headers.setRange(Arrays.asList(ranges));
            return this;
        }

        public MutatedServerHttpRequest build() {
            return this.body((Publisher)Flux.empty());
        }

        public MutatedServerHttpRequest body(String body) {
            return this.body((Publisher)Flux.just(BUFFER_FACTORY.wrap(body.getBytes(this.getCharset()))));
        }

        private Charset getCharset() {
            return (Charset) Optional.ofNullable(this.headers.getContentType()).map(MimeType::getCharset).orElse(StandardCharsets.UTF_8);
        }

        public MutatedServerHttpRequest body(Publisher<? extends DataBuffer> body) {
            this.applyCookiesIfNecessary();
            return new MutatedServerHttpRequest(this.method, this.getUrlToUse(), this.contextPath, this.headers, this.cookies, this.remoteAddress, this.sslInfo, body);
        }

        private void applyCookiesIfNecessary() {
            if (this.headers.get("Cookie") == null) {
                this.cookies.values().stream().flatMap(Collection::stream).forEach((cookie) -> {
                    this.headers.add("Cookie", cookie.toString());
                });
            }

        }

        private URI getUrlToUse() {
            MultiValueMap<String, String> params = this.queryParamsBuilder.buildAndExpand(new Object[0]).encode().getQueryParams();
            return !params.isEmpty() ? UriComponentsBuilder.fromUri(this.url).queryParams(params).build(true).toUri() : this.url;
        }
    }

    public interface BodyBuilder extends MutatedServerHttpRequest.BaseBuilder<MutatedServerHttpRequest.BodyBuilder> {
        MutatedServerHttpRequest.BodyBuilder contentLength(long var1);

        MutatedServerHttpRequest.BodyBuilder contentType(MediaType var1);

        MutatedServerHttpRequest body(Publisher<? extends DataBuffer> var1);

        MutatedServerHttpRequest body(String var1);
    }

    public interface BaseBuilder<B extends MutatedServerHttpRequest.BaseBuilder<B>> {
        B contextPath(String var1);

        B queryParam(String var1, Object... var2);

        B queryParams(MultiValueMap<String, String> var1);

        B remoteAddress(InetSocketAddress var1);

        void sslInfo(SslInfo var1);

        B cookie(HttpCookie... var1);

        B cookies(MultiValueMap<String, HttpCookie> var1);

        B header(String var1, String... var2);

        B headers(MultiValueMap<String, String> var1);

        B accept(MediaType... var1);

        B acceptCharset(Charset... var1);

        B acceptLanguageAsLocales(Locale... var1);

        B ifModifiedSince(long var1);

        B ifUnmodifiedSince(long var1);

        B ifNoneMatch(String... var1);

        B range(HttpRange... var1);

        MutatedServerHttpRequest build();
    }
}
