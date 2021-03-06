package net.nemerosa.ontrack.jenkins;

import hudson.model.ParameterDefinition;
import net.nemerosa.ontrack.jenkins.dsl.DSLRunner;
import net.nemerosa.ontrack.jenkins.dsl.OntrackDSLRunner;
import org.apache.commons.beanutils.BeanUtils;

public abstract class AbstractOntrackParameterDefinition extends ParameterDefinition {

    private final String dsl;
    private final boolean sandbox;
    private final String valueProperty;
    private final transient DSLRunner dslRunner;

    public AbstractOntrackParameterDefinition(String name, String description, String dsl, boolean sandbox, String valueProperty) {
        this(
                name,
                description,
                dsl,
                sandbox,
                valueProperty,
                null
        );
    }

    public AbstractOntrackParameterDefinition(String name, String description, String dsl, boolean sandbox, String valueProperty, DSLRunner dslRunner) {
        super(name, description);
        this.dsl = dsl;
        this.valueProperty = valueProperty;
        this.dslRunner = dslRunner;
        this.sandbox = sandbox;
    }

    public String getDsl() {
        return dsl;
    }

    public String getValueProperty() {
        return valueProperty;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    protected Object runDSL() {
        DSLRunner runner = dslRunner != null ? dslRunner : createDSLRunner();
        return runner.run(dsl);
    }

    protected DSLRunner createDSLRunner() {
        return OntrackDSLRunner.getRunner().setSandbox(sandbox);
    }

    protected String getProperty(Object any, String property) {
        try {
            return BeanUtils.getProperty(any, property);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Could not get property %s on object %s",
                            property,
                            any
                    )
            );
        }
    }

}
