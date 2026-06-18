package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class CountableFileBody extends FileBody {
    private StreamListener streamListener;

    public CountableFileBody(File file, ContentType contentType, String filename) {
        super(file, contentType, filename);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        CountingOutputStream output = new CountingOutputStream(out) {
            @Override
            protected synchronized void beforeWrite(int n) {
                if (streamListener != null && n != 0)
                    streamListener.counterChanged(n);
                super.beforeWrite(n);
            }
        };
        super.writeTo(output);
    }

    public void setStreamListener(StreamListener listener) {
        streamListener = listener;
    }

    public StreamListener getStreamListener() {
        return streamListener;
    }
}
