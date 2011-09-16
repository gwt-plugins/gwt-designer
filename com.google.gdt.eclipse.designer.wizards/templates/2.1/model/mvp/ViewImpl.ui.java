/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package %packageName%;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sample implementation of {@link %viewName%}.
 */
public class %className% extends Composite implements %viewName% {

	interface Binder extends UiBinder<Widget, %className%> {
	}
	
	private static final Binder binder = GWT.create(Binder.class);

	private Presenter listener;
	@UiField
	Button button;

	public %className%() {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setName(String name) {
		button.setHTML(name);
	}

	@Override
	public void setPresenter(Presenter listener) {
		this.listener = listener;
	}

	@UiHandler("button")
	void onButtonClick(ClickEvent event) {
		Place newPlace = null;
		// TODO
		listener.goTo(newPlace);
	}
}
