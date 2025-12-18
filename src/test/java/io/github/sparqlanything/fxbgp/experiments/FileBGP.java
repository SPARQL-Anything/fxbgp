package io.github.sparqlanything.fxbgp.experiments;

import org.apache.commons.io.IOUtils;
import org.apache.jena.sparql.algebra.op.OpBGP;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileBGP {
    private File file;
    private List<OpBGP> bgps;
    private String query;
    private String sourceURL;
    FileBGP(File file, String query, List<OpBGP> bgps, String sourceURL) {
        this.file = file;
        this.query = query;
        this.bgps = bgps;
        this.sourceURL = sourceURL;
    }
    public static FileBGP make(File file) throws IOException {
        String query = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8.name());
        // Get the first line of the string query
        String first = query.split("\n")[0];
        return new FileBGP(file, query, FXBGPUtils.extract(query), first);
    }

    public List<OpBGP> getBgps() {
        return bgps;
    }

    public File getFile() {
        return file;
    }

    public String getName(){
        return file.getName();
    }

    public String getQuery() {
        return query;
    }

    public String getSourceURL() {
        return file.toURI().toString();
    }
}
