package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.IconUtils;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class GFileDialog extends JFileChooser {

    public static final FileNameExtensionFilter TEXT_FILES = new FileNameExtensionFilter("Text file", "txt");
    public static final FileNameExtensionFilter PEMU_FILES = new FileNameExtensionFilter("PEMU program", "pemu");
    public static final ImageIcon ICON_SAVE = IconUtils.importIcon("/assets/save.png", Application.MENU_ITEM_ICON_SIZE);
    public static final ImageIcon ICON_OPEN = IconUtils.importIcon("/assets/open_file.png", Application.MENU_ITEM_ICON_SIZE);

    private static GFileDialog INSTANCE;

    private GFileDialog() {
        super();

        setCurrentDirectory(new File("./"));
        setMultiSelectionEnabled(false);
        setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    public static @NotNull GFileDialog getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GFileDialog();
        return INSTANCE;
    }

    public int showOpenDialog(Component parent, FileNameExtensionFilter filter) throws HeadlessException {
        resetChoosableFileFilters();
        setFileFilter(filter);
        return showOpenDialog(parent);
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        setDialogTitle("Open");
        return super.showOpenDialog(parent);
    }

    public int showSaveDialog(Component parent, FileNameExtensionFilter filter) throws HeadlessException {
        resetChoosableFileFilters();
        setFileFilter(filter);

        return showSaveDialog(parent);
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        setDialogTitle("Save As");
        return super.showSaveDialog(parent);
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        switch (getDialogType()) {
            case SAVE_DIALOG:
                dialog.setIconImage(ICON_SAVE.getImage());
                break;
            case OPEN_DIALOG:
                dialog.setIconImage(ICON_OPEN.getImage());
                break;
        }
        return dialog;
    }

    @Override
    public void approveSelection() {
        if (getDialogType() == SAVE_DIALOG) {
            File file = new File(StringUtils.getFilePathWExt(getSelectedFile(), StringUtils.getFileExtFromFilter(getFileFilter())));
            setSelectedFile(file);

            if (file.exists()) {
                if (file.canWrite()) {
                    if (JOptionPane.showConfirmDialog(
                            this, file.getName() + " already exists.\nDo you want to overwrite it?",
                            "Confirm " + getDialogTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
                        ) == JOptionPane.OK_OPTION) super.approveSelection();
                } else
                    JOptionPane.showMessageDialog(
                            this, file.getName() + '\n' + (file.canRead() ? "File is read-only." : "File is locked.") + "\nTry with another name.",
                            getDialogTitle(), JOptionPane.WARNING_MESSAGE
                    );
            } else super.approveSelection();
        } else super.approveSelection();
    }
}
