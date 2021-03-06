package com.kartoflane.superluminal2.ui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kartoflane.superluminal2.Superluminal;
import com.kartoflane.superluminal2.components.enums.OS;
import com.kartoflane.superluminal2.core.Cache;
import com.kartoflane.superluminal2.core.Database;
import com.kartoflane.superluminal2.core.DatabaseEntry;
import com.kartoflane.superluminal2.utils.UIUtils;
import com.kartoflane.superluminal2.utils.Utils;

import org.eclipse.swt.widgets.Combo;

public class SaveOptionsDialog {
	private static SaveOptionsDialog instance = null;
	private static String prevPath = System.getProperty("user.home");

	private File resultFile = null;
	private DatabaseEntry resultMod = null;

	private Shell shell = null;
	private Button btnCancel;
	private Button btnConfirm;
	private Label lblSaveLocation;
	private Button btnBrowse;
	private Text txtDestination;
	private Button btnDirectory;
	private Group grpSaveAs;
	private Button btnFTL;
	private Label lblDirectoryHelp;
	private Label lblArchiveHelp;
	private Label lblInclude;
	private Combo cmbInclude;
	private Label lblSeparator2;
	private Label lblIncludeInfo;
	private Label lblSeparator1;

	public SaveOptionsDialog(Shell parent) {
		if (instance != null)
			throw new IllegalStateException("Previous instance has not been disposed!");
		instance = this;
		Image helpImage = Cache.checkOutImage(this, "cpath:/assets/help.png");

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(Superluminal.APP_NAME + " - Save Options");
		shell.setLayout(new GridLayout(2, false));

		grpSaveAs = new Group(shell, SWT.NONE);
		grpSaveAs.setText("Save as...");
		grpSaveAs.setLayout(new GridLayout(2, false));
		grpSaveAs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		btnDirectory = new Button(grpSaveAs, SWT.RADIO);
		btnDirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnDirectory.setText("Resource folder");

		lblDirectoryHelp = new Label(grpSaveAs, SWT.NONE);
		lblDirectoryHelp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDirectoryHelp.setImage(helpImage);
		String msg = "Saves the ship as a series of folders mirroring the internal " +
				"structure of the game's files -- source code for your mod, so to say.";
		UIUtils.addTooltip(lblDirectoryHelp, Utils.wrapOSNot(msg, Superluminal.WRAP_WIDTH, Superluminal.WRAP_TOLERANCE, OS.MACOSX()));

		btnFTL = new Button(grpSaveAs, SWT.RADIO);
		btnFTL.setText("FTL file");

		lblArchiveHelp = new Label(grpSaveAs, SWT.NONE);
		lblArchiveHelp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblArchiveHelp.setImage(helpImage);
		msg = "Saves the ship as a ready-to-install .ftl archive.";
		UIUtils.addTooltip(lblArchiveHelp, msg);

		lblSeparator1 = new Label(shell, SWT.NONE);
		lblSeparator1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		lblInclude = new Label(shell, SWT.NONE);
		lblInclude.setText("Include mod files from:");

		lblIncludeInfo = new Label(shell, SWT.NONE);
		lblIncludeInfo.setImage(helpImage);
		lblIncludeInfo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		cmbInclude = new Combo(shell, SWT.READ_ONLY);
		cmbInclude.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		cmbInclude.add("None");
		cmbInclude.select(0);

		msg = "Selecting a mod here will bundle it with your ship.";

		Database db = Database.getInstance();
		DatabaseEntry[] des = db.getEntries();
		if (des.length <= 1) {
			cmbInclude.setEnabled(false);
			msg += "\nDisabled: no mods are currently loaded.";
		}
		else {
			for (DatabaseEntry de : des) {
				if (de == db.getCore())
					continue;
				cmbInclude.add(de.getName());
			}
		}
		UIUtils.addTooltip(lblIncludeInfo, msg);

		lblSeparator2 = new Label(shell, SWT.NONE);
		lblSeparator2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		lblSaveLocation = new Label(shell, SWT.NONE);
		lblSaveLocation.setText("Save location:");

		btnBrowse = new Button(shell, SWT.NONE);
		btnBrowse.setEnabled(false);
		GridData gd_btnBrowse = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnBrowse.widthHint = 80;
		btnBrowse.setLayoutData(gd_btnBrowse);
		btnBrowse.setText("Browse");

		txtDestination = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		txtDestination.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		btnConfirm = new Button(shell, SWT.NONE);
		btnConfirm.setEnabled(false);
		GridData gd_btnConfirm = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnConfirm.widthHint = 80;
		btnConfirm.setLayoutData(gd_btnConfirm);
		btnConfirm.setText("Confirm");

		btnCancel = new Button(shell, SWT.NONE);
		GridData gd_btnCancel = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnCancel.widthHint = 80;
		btnCancel.setLayoutData(gd_btnCancel);
		btnCancel.setText("Cancel");

		btnDirectory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnBrowse.setEnabled(true);
				resultFile = null;
				txtDestination.setText("");
				btnConfirm.setEnabled(false);
			}
		});

		btnFTL.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnBrowse.setEnabled(true);
				resultFile = null;
				txtDestination.setText("");
				btnConfirm.setEnabled(false);
			}
		});
		
		cmbInclude.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = cmbInclude.getSelectionIndex();
				if (i == 0) {
					resultMod = null;
				}
				else {
					Database db = Database.getInstance();
					resultMod = db.getEntries()[i];
				}
			}
		});

		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File temp = null;
				if (btnDirectory.getSelection()) {
					temp = UIUtils.promptForDirectory(shell, "Save Ship as Directory", "Please select the directory to which the ship will be exported.", prevPath);
				} else {
					temp = UIUtils.promptForSaveFile(shell, "Save Ship as FTL", prevPath, new String[] { "*.ftl", "*.zip" });
				}

				// User could've aborted selection, which returns null.
				if (temp != null) {
					prevPath = temp.getAbsolutePath();
					resultFile = temp;
					txtDestination.setText(temp.getAbsolutePath());
				}

				btnConfirm.setEnabled(resultFile != null);
			}
		});

		btnConfirm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dispose();
			}
		});

		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resultFile = null;
				dispose();
			}
		});

		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event e) {
				btnCancel.notifyListeners(SWT.Selection, null);
				e.doit = false;
			}
		});

		shell.pack();
		Point size = shell.getSize();
		size.x = 300;
		shell.setSize(300, size.y);
		Point parSize = parent.getSize();
		Point parLoc = parent.getLocation();
		shell.setLocation(parLoc.x + parSize.x / 3 - size.x / 2, parLoc.y + parSize.y / 3 - size.y / 2);
	}

	public SaveOptions open() {
		Display display = Display.getDefault();

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return new SaveOptions(resultFile, resultMod);
	}

	public static SaveOptionsDialog getInstance() {
		return instance;
	}

	public boolean isActive() {
		return !shell.isDisposed();
	}

	public void dispose() {
		Cache.checkInImage(this, "cpath:/assets/help.png");
		shell.dispose();
		instance = null;
	}

	public static class SaveOptions {
		public final File file;
		public final DatabaseEntry mod;

		public SaveOptions(File f) {
			file = f;
			mod = null;
		}

		public SaveOptions(File f, DatabaseEntry de) {
			file = f;
			mod = de;
		}

		public SaveOptions(DatabaseEntry de) {
			mod = de;
			file = de.getFile();
		}
	}
}
