package io.ballerina.openapi.generators.common;

import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DocCommenTests {
    @Test()
    public void testForFunctionReplaceContentWithinBrackets() {
        // todo : need ot improve regex to capture `.` in string ex: abc.json
//        Assert.assertEquals("admin/api/ZZZ/customers/XXX/ZZZ",
//        GeneratorUtils.replaceContentWithinBrackets("admin/api/2021-10/customers/{customer_id}/abc.json", "XXX"));
//        Assert.assertEquals("admin/api/ZZZ/customers/XXX/ZZZ",
//                GeneratorUtils.replaceContentWithinBrackets(
//        "admin/api/'2021\\-10/customers/[string customer_id]/abc\\.json", "XXX"));

        Assert.assertEquals("/user/ssh_signing_keys/XXX", GeneratorUtils.replaceContentWithinBrackets(
                "/user/ssh_signing_keys/{ssh_signing_key_id}", "XXX"));
        Assert.assertEquals("/user/gpg_keys/XXX", GeneratorUtils.replaceContentWithinBrackets(
                "/user/gpg_keys/{gpg_key_id}", "XXX"));

        Assert.assertEquals(GeneratorUtils.replaceContentWithinBrackets("user/gpg_keys/[int gpg_key_id]",
                "XXX"), "user/gpg_keys/XXX");
        Assert.assertEquals(GeneratorUtils.replaceContentWithinBrackets(
                "user/ssh_signing_keys/[int ssh_signing_key_id]", "XXX"),
                "user/ssh_signing_keys/XXX");
    }
}
