package ru.seloid.pdf.merger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.seloid.pdf.merger.windows.MainWindow;

import javax.swing.*;

public class DesktopApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopApp.class);

    private static final String STYLE_NAME = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

    public static void main(final String[] args) {
        LOGGER.debug("PdfMerger started");
        applyStyle(STYLE_NAME);
        SwingUtilities.invokeLater(MainWindow::run);
    }

    private static void applyStyle(final String styleName) {
        try {
            UIManager.setLookAndFeel(styleName);
        } catch (final Exception e) {
            LOGGER.error("Problem to apply style", e);
        }
    }
}
