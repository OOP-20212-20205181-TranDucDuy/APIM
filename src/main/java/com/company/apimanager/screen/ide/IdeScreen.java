package com.company.apimanager.screen.ide;

import com.company.apimanager.entity.KongPluginType;
import com.company.apimanager.entity.RestApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("IdeScreen")
@UiDescriptor("ide-screen.xml")
public class IdeScreen extends Screen {
    static private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void setContent(RestApi rest, KongPluginType type){
        try {
            String content = "";
            if(type.equals(KongPluginType.REQUEST_TRANSFORMER)){
                content = rest.getRequestTransformer();
            }
            if(type.equals(KongPluginType.RESPONSE_TRANSFORMER)){
                content = rest.getResponseTransformer();
            }
            sourceCodeEditor.setValue(mapper.readTree(content).toPrettyString());
        }catch (Exception ex){

        }
    }
    @Autowired
    private SourceCodeEditor sourceCodeEditor;
    private String result;
    @Autowired
    private Button okBtn;

    public String getResult() {
        return result;
    }

    @Autowired
    private Button cancelBtn;

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {

    }

    @Subscribe("cancelBtn")
    public void onCancelBtnClick(Button.ClickEvent event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("okBtn")
    public void onOkBtnClick(Button.ClickEvent event) {
        result = sourceCodeEditor.getRawValue();
        close(StandardOutcome.COMMIT);
    }

}