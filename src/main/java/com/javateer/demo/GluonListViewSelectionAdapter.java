package com.javateer.demo;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.gluonhq.charm.glisten.control.CharmListView;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;

public class GluonListViewSelectionAdapter<T, R> implements Consumer<Event> {

    private T listCell;

    private Class<T> tClass;

    private Function<T,R> listItemMapper;

    private Consumer<R> selected, deselected;

    private MultipleSelectionModel<R> multipleSelectionModel;

    private ObservableList<Integer> selectedIndices;

    /*
     * Samsung devices cause a discrepancy for GluonFX event notification resulting in touch-click events and
     * touch-drag events both being observed as a move, not a tap. So attempting to select a list item works on
     * Android devices but not Samsung. The workaround solution to select list items requires dismissing the system
     * raising mouse-click events on the list when the User is just scrolling the list,
     * which is the purpose of this flag.
     */
    private boolean isScrollingFinished = false;

    @SuppressWarnings("unused")
    private GluonListViewSelectionAdapter() {}

    public GluonListViewSelectionAdapter(
            Class<T> tClass,
            ListView<R> innerList,
            Function<T,R> listItemMapper,
            Consumer<R> selected,
            Consumer<R> deselected,
            ObservableList<Integer> selectedIndices) {

        this.tClass = tClass;
        this.listItemMapper = listItemMapper;
        this.selected = selected;
        this.deselected = deselected;
        this.selectedIndices = selectedIndices;
        this.multipleSelectionModel = innerList.getSelectionModel();

        innerList.addEventFilter(MouseEvent.MOUSE_CLICKED, (mouseEvent) -> {

            if(isScrollingFinished)
                isScrollingFinished = false; // reset for User's next possible scroll event.
            else
                accept(mouseEvent);

            mouseEvent.consume();
        });

        innerList.setOnScrollFinished((scrollEvent) -> isScrollingFinished = true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void accept(Event e) {

        Integer selectedIndex = multipleSelectionModel.getSelectedIndex();

        // we need to determine the selected index for after this braced code block
        if (com.gluonhq.attach.util.Platform.isAndroid()) {

            Optional<T> optionalT = findT(e);
            if (optionalT.isEmpty()) return;
            listCell = optionalT.get();


            // first, backup the current list of selected indexes
            List<Integer> list = multipleSelectionModel.getSelectedIndices();
            // second, set the selected object to learn its index position
            multipleSelectionModel.clearSelection();
            multipleSelectionModel.select(listItemMapper.apply(listCell));
            selectedIndex = multipleSelectionModel.getSelectedIndex();
            if (isHeaderRow(selectedIndex, listItemMapper.apply(listCell))) ++selectedIndex;
            // third, restore everything that was selected before, including this last, newly made selection.
            multipleSelectionModel.clearSelection();
            for(int listedIndex : list) multipleSelectionModel.selectIndices(listedIndex);
            multipleSelectionModel.select(listItemMapper.apply(listCell));
        }

        if(selectedIndex == -1) return;

        this.listCell = (T) e.getTarget();
        R itemClicked = multipleSelectionModel.getSelectedItem();

        if (isHeaderRow(selectedIndex, itemClicked)) {
            multipleSelectionModel.clearSelection(selectedIndex);
        }
        else if (selectedIndices.contains(selectedIndex)) {
            selectedIndices.remove(selectedIndex);
            deselected.accept(itemClicked);
            multipleSelectionModel.clearSelection(selectedIndex);
        }
        else {
            selectedIndices.add(selectedIndex);
            selected.accept(itemClicked);
        }

        if (multipleSelectionModel.getSelectionMode().equals(SelectionMode.MULTIPLE))
            for (int index : selectedIndices) multipleSelectionModel.selectIndices(index);

        @SuppressWarnings("rawtypes")
        Optional<CharmListView> charmListView = getCharmListView();
        if (charmListView.isPresent()) {
            charmListView.get().layout(); // without this, list items flicker when user selects/deselects them.
        }
    }

    private Optional<T> findT(Event event) {

        Node node = (Node) event.getTarget();
        T listCell = null;
        while(listCell == null) {
            try {
                listCell = (tClass.cast(node));
            }
            catch(ClassCastException cce) {
                node = node.getParent();
                if (node == null) break;
            }
        }
        return Optional.ofNullable(listCell);
    }

    @SuppressWarnings("rawtypes")
    private boolean isHeaderRow(Integer selectedIndex, R itemClicked) {

        boolean result = false;

        Optional<CharmListView> wrapper = getCharmListView();
        if (wrapper.isEmpty()) return result;

        int firstIndex = wrapper.get().getHeadersList().indexOf(itemClicked);
        int lastIndex = wrapper.get().getHeadersList().lastIndexOf(itemClicked);
        result = (firstIndex != lastIndex) && (selectedIndex == firstIndex);

        return result;
    }

    @SuppressWarnings("rawtypes")
    private Optional<CharmListView> getCharmListView() {

        Node node = (Node) listCell;
        CharmListView wrapper = null;
        while(wrapper == null) {
            try {
                wrapper = ((CharmListView) node);
            }
            catch(ClassCastException cce) {
                node = node.getParent();
                if (node == null) return Optional.empty();
            }
        }
        return Optional.of(wrapper);
    }
}
