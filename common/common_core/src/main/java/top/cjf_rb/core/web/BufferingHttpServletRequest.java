package top.cjf_rb.core.web;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import top.cjf_rb.core.util.Webs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 缓冲request body, 使之可以多次读取body内容

 @author lty
 @since 1.0 */
public class BufferingHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] body;

    public BufferingHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 表单类型不处理
        if (this.isForm()) {
            return super.getInputStream();
        }

        if (Objects.isNull(body)) {
            this.body = StreamUtils.copyToByteArray(super.getInputStream());
        }

        return new BufferingServletInputStream(body);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // 表单类型不处理
        if (this.isForm()) {
            return super.getReader();
        }

        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    /**
     是否表单提交
     */
    private boolean isForm() {
        return Webs.isFormRequest((HttpServletRequest) getRequest());
    }

    /**

     */
    public static class BufferingServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;
        private boolean isFinished = false;

        BufferingServletInputStream(byte[] body) {
            Objects.requireNonNull(body);
            inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            int read = inputStream.read();
            if (read == -1) {
                isFinished = true;
            }

            return read;
        }

        @Override
        public boolean isFinished() {
            return isFinished;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) {
            return this.inputStream.read(b, off, len);
        }

        @Override
        public int read(@NonNull byte[] b) throws IOException {
            return this.inputStream.read(b);
        }

        @Override
        public long skip(long n) {
            return this.inputStream.skip(n);
        }

        @Override
        public int available() {
            return this.inputStream.available();
        }

        @Override
        public void close() throws IOException {
            this.inputStream.close();
        }

        @Override
        public synchronized void mark(int readLimit) {
            this.inputStream.mark(readLimit);
        }

        @Override
        public synchronized void reset() {
            this.inputStream.reset();
        }

        @Override
        public boolean markSupported() {
            return this.inputStream.markSupported();
        }

    }
}
