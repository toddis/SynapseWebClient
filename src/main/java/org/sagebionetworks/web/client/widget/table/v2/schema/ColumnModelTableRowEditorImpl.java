package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;

import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Editor implementation of ColumnModelTableRow
 * 
 * @author John
 *
 */
public class ColumnModelTableRowEditorImpl extends AbstractColumnModelTableRow implements ColumnModelTableRowEditor {
	
	public interface Binder extends UiBinder<TableRow, ColumnModelTableRowEditorImpl> {	}
	@UiField
	TextBox name;
	@UiField
	Select type;
	@UiField
	TextBox maxSize;
	@UiField
	TextBox defaultValue;
	@UiField
	TextBox restrictValues;
	String id;
	TypePresenter presenter;
	
	@Inject
	public ColumnModelTableRowEditorImpl(Binder uiBinder){
		row = uiBinder.createAndBindUi(this);
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getColumnName() {
		return name.getText();
	}

	@Override
	public ColumnTypeViewEnum getColumnType() {
		return ColumnTypeViewEnum.valueOf(type.getValue());
	}

	@Override
	public String getMaxSize() {
		return maxSize.getText();
	}

	@Override
	public String getDefaultValue() {
		return defaultValue.getText();
	}

	@Override
	public void setTypePresenter(TypePresenter presenterIn) {
		this.presenter = presenterIn;
		type.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.onTypeChanged();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return row;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setColumnType(ColumnTypeViewEnum type) {
		this.type.setValue(type.name());
	}

	@Override
	public void setColumnName(String name) {
		this.name.setText(name);
	}

	@Override
	public void setMaxSize(String maxSize) {
		this.maxSize.setText(maxSize);
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue.setText(defaultValue);
	}

	@Override
	public void setSizeFieldVisible(boolean visible) {
		maxSize.setVisible(visible);
	}

	@Override
	public void setEnumValues(List<String> enums) {
		restrictValues.setText(ColumnModelUtils.listToCSV(enums));
	}

	@Override
	public List<String> getEnumValues() {
		return ColumnModelUtils.csvToList(restrictValues.getText());
	}

	@Override
	public IsWidget getWidget(int index) {
		switch (index) {
		case 0:
			return name;
		case 1:
			return type;
		case 2:
			return maxSize;
		case 3:
			return defaultValue;
		case 4:
			return restrictValues;
		default:
			throw new IllegalArgumentException("Unknown index: "+index);
		}
	}

	@Override
	public int getWidgetCount() {
		return 5;
	}
}
