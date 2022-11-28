package com.javateer.demo;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.scene.control.ListCell;

public class IconListCell extends ListCell<MaterialDesignIcon> {

    private MaterialDesignIcon materialDesignIcon;

    @Override
    public void updateItem(MaterialDesignIcon materialDesignIcon, boolean isEmpty) {

        super.updateItem(materialDesignIcon, isEmpty);

        this.materialDesignIcon = materialDesignIcon;

        if (isEmpty || materialDesignIcon == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            setText(materialDesignIcon.name());
            setGraphic(materialDesignIcon.graphic());
        }
    }

    public MaterialDesignIcon getIcon() {
        return this.materialDesignIcon;
    }
}
