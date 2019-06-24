package application.ui.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

/**
 * Provides a controller for the navigator view.
 * @author Joel Håkansson
 * 
 */
public class NavigatorToolController extends BorderPane {
	private static final Logger logger = Logger.getLogger(NavigatorToolController.class.getCanonicalName());
	private static final Image drawerIcon = new Image(NavigatorToolController.class.getResourceAsStream("resource-files/drawer.png")); 
	private static final Image folderIcon = new Image(NavigatorToolController.class.getResourceAsStream("resource-files/folder.png"));
	private static final Image fileIcon = new Image(NavigatorToolController.class.getResourceAsStream("resource-files/file.png"));
	TreeItem<PathInfo> rootNode = 
			new FileTreeItem();
	@FXML TreeView<PathInfo> tree;
	private final Consumer<Path> action;

	/**
	 * Creates a new search view controller.
	 */
	public NavigatorToolController(Consumer<Path> action) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NavigatorTool.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		this.action = action;
	}
	
	@FXML void initialize() {
		tree.setShowRoot(false);
		tree.setOnMouseClicked(ev->{
			if (ev.getClickCount()>=2) {
				openSelectedItem();
			}
		});
		tree.setOnKeyPressed(t->{
			if (t.getCode()==KeyCode.DELETE) {
				//TODO: null check
				PathInfo pathInfo = tree.getSelectionModel().getSelectedItem().getValue();
				if (pathInfo.isRoot()) {
					removePath(pathInfo);
				} else {
					// ask to delete file
				}
			} else if (t.getCode()==KeyCode.ENTER) {
				openSelectedItem();
			}
		});
		setRootNode(rootNode);
	}
	
	private void openSelectedItem() {
		TreeItem<PathInfo> item = tree.getSelectionModel().getSelectedItem();
		if (item!=null && item.getValue()!=null && !Files.isDirectory(item.getValue().getPath())) {
			action.accept(item.getValue().getPath());
		}
	}
	
	private void setRootNode(TreeItem<PathInfo> root) {
		tree.setRoot(root);
	}

	public void addPath(Path p) {
		if (Files.isDirectory(p)) {
			FileTreeItem depNode = new FileTreeItem(
					new PathInfo(p, true), 
					new ImageView(drawerIcon)
				);
			depNode.expandedProperty().addListener(createChangeListener(p, depNode));
			rootNode.getChildren().add(depNode);
			setPath(p, depNode);
		}
	}
	
	public void removePath(PathInfo p) {
		rootNode.getChildren().removeIf(v->v.getValue().equals(p));
	}
	
	private static void setPath(Path p, FileTreeItem node) {
		try {
			Files.list(p)
			.sorted((p1, p2)->{
				if (Files.isDirectory(p1) && !Files.isDirectory(p2)) {
					return -1;
				} else if (!Files.isDirectory(p1) && Files.isDirectory(p2)) {
					return 1;
				} else {
					// Both are files or both are directories
					return p1.compareTo(p2);
				}
				})
			.forEach(v-> {
				if (Files.isDirectory(v)) {
					FileTreeItem empLeaf = new FileTreeItem(v, new ImageView(folderIcon));
					empLeaf.expandedProperty().addListener(createChangeListener(v, empLeaf));
					node.getChildren().add(empLeaf);
				} else {
					FileTreeItem empLeaf = new FileTreeItem(v, new ImageView(fileIcon));
					node.getChildren().add(empLeaf);
				}
				
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static ChangeListener<? super Boolean> createChangeListener(Path v, FileTreeItem empLeaf) {
		return (o, ov, nv)->{
			if (nv.booleanValue()) {
				empLeaf.getChildren().clear();
				setPath(v, empLeaf);
			}
		};
	}
	
	private static class PathInfo {
		private final Path path;
		private final boolean root;
		
		PathInfo(Path p) {
			this.path = p;
			this.root = false;
		}
		
		PathInfo(Path p, boolean root) {
			this.path = p;
			this.root = root;
		}
		

		@Override
		public String toString() {
			return root?path.toString():path.getName(path.getNameCount()-1).toString();
		}

		public Path getPath() {
			return path;
		}
		
		public boolean isRoot() {
			return root;
		}
		
	}
	
	public static class FileTreeItem extends TreeItem<PathInfo> {

		/**
		 * 
		 */
		public FileTreeItem() {
			// TODO Auto-generated constructor stub
		}

		/**
		 * @param value
		 * @param graphic
		 */
		public FileTreeItem(PathInfo value, Node graphic) {
			super(value, graphic);
		}
		
		public FileTreeItem(Path value, Node graphic) {
			super(new PathInfo(value), graphic);
		}

		/**
		 * @param value
		 */
		public FileTreeItem(Path value) {
			super(new PathInfo(value));
		}

		@Override
		public boolean isLeaf() {
			return Optional.ofNullable(getValue()).map(v->!Files.isDirectory(v.getPath())).orElse(false);
		}
		
		
		// Implement isLeaf here to support folders properly
	}

}
