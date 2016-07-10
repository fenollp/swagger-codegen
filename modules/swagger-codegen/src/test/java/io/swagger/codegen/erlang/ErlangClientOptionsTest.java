package io.swagger.codegen.erlang;

import io.swagger.codegen.AbstractOptionsTest;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.languages.ErlangClientCodegen;
import io.swagger.codegen.options.ErlangClientOptionsProvider;

import mockit.Expectations;
import mockit.Tested;

public class ErlangClientOptionsTest extends AbstractOptionsTest {

    @Tested
    private ErlangClientCodegen clientCodegen;

    public ErlangClientOptionsTest() {
        super(new ErlangClientOptionsProvider());
    }

    @Override
    protected CodegenConfig getCodegenConfig() {
        return clientCodegen;
    }

    @SuppressWarnings("unused")
    @Override
    protected void setExpectations() {
        new Expectations(clientCodegen) {{
            clientCodegen.setPackageVersion(ErlangClientOptionsProvider.PACKAGE_VERSION_VALUE);
            times = 1;
            clientCodegen.setPackageName(ErlangClientOptionsProvider.PACKAGE_NAME_VALUE);
            times = 1;
        }};
    }
}
