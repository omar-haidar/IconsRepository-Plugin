package dev.omar.plugin.iconsrepo.data.importer;

import java.io.OutputStream;

public interface IconImporter {
    ImportIconResult importIcon(IconSource source, OutputStream outputStream);
}
