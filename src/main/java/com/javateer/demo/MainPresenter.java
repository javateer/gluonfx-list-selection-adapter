package com.javateer.demo;



import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.StackPane;

public class MainPresenter {

    @FXML
    private ResourceBundle resources;

    @FXML
    private View view;

    @FXML
    private StackPane defaultTab;

    @FXML
    private Label defaultTabLabel;

    @FXML
    private StackPane adaptedTab;

    @FXML
    private Label adaptedTabLabel;

    @FXML
    private ListView<MaterialDesignIcon> defaultListView;

    @FXML
    private ListView<MaterialDesignIcon> adaptedListView;

    private Set<Integer> defaultListSelectedIndices = new HashSet<>();

    private final boolean showDefaultListView = true;

    public void initialize() {

        initAppBar();

        initTabPane();

        initListViews();
    }

    private void initAppBar() {

        view.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Touch Selection Adapter Demo");
            }
        });
    }

    private void initTabPane() {

        defaultTabLabel.setText(resources.getString("view.backup-tab.label"));
        adaptedTabLabel.setText(resources.getString("view.restore-tab.label"));

        defaultTabLabel.setStyle("-fx-opacity: 1.0");
        adaptedTabLabel.setStyle("-fx-opacity: 0.6");

        defaultTab.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            selectDefaultTab();
            toggleListView(showDefaultListView);
        });
        adaptedTab.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            selectAdaptedTab();
            toggleListView(!showDefaultListView);
        });

        // support user toggling which tab is acive with left/right swipes.
        view.addEventFilter(SwipeEvent.SWIPE_RIGHT, (swipeEvent) -> {
            if(!isDefaultTabActive()) {
                selectDefaultTab();
                toggleListView(showDefaultListView);
            }
            swipeEvent.consume();
        });
        view.addEventFilter(SwipeEvent.SWIPE_LEFT, (swipeEvent) -> {
            if(isDefaultTabActive()) {
                selectAdaptedTab();
                toggleListView(!showDefaultListView);
            }
            swipeEvent.consume();
        });

        toggleListView(showDefaultListView);
    }

    private void initListViews() {

        initDefaultListView();
        initAdaptedListView();
    }

    private void initDefaultListView() {

        defaultListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        defaultListView.setCellFactory(arg -> new IconListCell());

        defaultListView.addEventFilter(MouseEvent.MOUSE_CLICKED, (mouseEvent) -> {

            mouseEvent.consume();

            MultipleSelectionModel<MaterialDesignIcon> selectionModel = defaultListView.getSelectionModel();
            Integer selectedIndex = selectionModel.getSelectedIndex();

            if(selectedIndex == -1) return;

            if(defaultListSelectedIndices.contains(selectedIndex)) {
                defaultListSelectedIndices.remove(selectedIndex);
                selectionModel.clearSelection(selectedIndex);
                System.out.println("Default List Item Deselected: " + defaultListView.getItems().get(selectedIndex));
                System.out.println("Total Default List Items Currently Selected: " + defaultListSelectedIndices.size());
                System.out.println();
            }
            else {
                defaultListSelectedIndices.add(selectedIndex);
                System.out.println("Default List Item Selected: " + defaultListView.getItems().get(selectedIndex));
                System.out.println("Total Default List Items Currently Selected: " + defaultListSelectedIndices.size());
                System.out.println();
            }

            if (selectionModel.getSelectionMode().equals(SelectionMode.MULTIPLE))
                for (int index : defaultListSelectedIndices) selectionModel.selectIndices(index);
        });

        Platform.runLater(() -> {
            ListProperty<MaterialDesignIcon> listProperty = new SimpleListProperty<>();
            listProperty.set(FXCollections.observableArrayList());
            listProperty.get().addAll(MaterialDesignIcon.values());
            defaultListView.itemsProperty().bind(listProperty);
        });
    }


    private void initAdaptedListView() {

        adaptedListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        adaptedListView.setCellFactory(arg -> new IconListCell());

        /*
         * This block of code is to configure the Gluon-bug workaround GluonListViewselectionAdapter for a ListView.
         */
        Function<IconListCell, MaterialDesignIcon> listItemMapper = ((iconListCell) -> iconListCell.getIcon());
        ObservableList<Integer> selectedIndicies = FXCollections.observableArrayList();
        Consumer<MaterialDesignIcon> listItemSelected = ((materialDesignIcon) -> {
            System.out.println("Adapted List Item Selected: " + materialDesignIcon.toString());
            System.out.println("Total Adapted List Items Currently Selected: " + selectedIndicies.size());
            System.out.println();
        });
        Consumer<MaterialDesignIcon> listItemDeselected = ((materialDesignIcon) -> {
            System.out.println("Adapted List Item Deselected: " + materialDesignIcon.toString());
            System.out.println("Total Adapted List Items Currently Selected: " + selectedIndicies.size());
            System.out.println();
        });
        new GluonListViewSelectionAdapter<IconListCell, MaterialDesignIcon>(
                IconListCell.class,
                adaptedListView,
                listItemMapper,
                listItemSelected,
                listItemDeselected,
                selectedIndicies);

        Platform.runLater(() -> {
            ListProperty<MaterialDesignIcon> listProperty = new SimpleListProperty<>();
            listProperty.set(FXCollections.observableArrayList());
            listProperty.get().addAll(MaterialDesignIcon.values());
            adaptedListView.itemsProperty().bind(listProperty);
        });
    }

    private void selectDefaultTab() {

        adaptedTab.getStyleClass().removeIf((styleClassName) -> styleClassName.equals("active-tab"));
        if (!defaultTab.getStyleClass().contains("active-tab"))
            defaultTab.getStyleClass().add("active-tab");

        defaultTabLabel.setStyle("-fx-opacity: 1.0"); adaptedTabLabel.setStyle("-fx-opacity: 0.6");
    }

    private void selectAdaptedTab() {

        defaultTab.getStyleClass().removeIf((styleClassName) -> styleClassName.equals("active-tab"));
        if (!adaptedTab.getStyleClass().contains("active-tab"))
            adaptedTab.getStyleClass().add("active-tab");

        adaptedTabLabel.setStyle("-fx-opacity: 1.0"); defaultTabLabel.setStyle("-fx-opacity: 0.6");
    }

    private boolean isDefaultTabActive() {

        return defaultTab.getStyleClass().contains("active-tab");
    }

    private void toggleListView(boolean showDefaultList) {

        if(showDefaultList) {
            defaultListView.setVisible(showDefaultList);
            adaptedListView.setVisible(!showDefaultList);
        }
        else {
            defaultListView.setVisible(showDefaultList);
            adaptedListView.setVisible(!showDefaultList);
        }
    }
}
