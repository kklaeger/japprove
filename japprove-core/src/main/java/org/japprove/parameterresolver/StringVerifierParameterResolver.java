package org.japprove.parameterresolver;

import org.japprove.config.ApprovalTestingConfiguration;
import org.japprove.engine.ApprovalTestingEngine;
import org.japprove.repositories.BaselineRepositoryImpl;
import org.japprove.verifier.StringVerifier;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@code StringVerifierParameterResolver} allows to use the {@link StringVerifier} as parameter in
 * Approval Tests.
 *
 * @see ParameterResolver
 */
public class StringVerifierParameterResolver extends ApprovalTestParameterResolver
        implements ParameterResolver {

    /**
     * Returns true if the {@link StringVerifier} is supported as a parameter.
     *
     * @see ParameterResolver
     */
    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == StringVerifier.class;
    }

    /**
     * Resolve an argument for the {@link StringVerifier}.
     *
     * @see ParameterResolver
     */
    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) {
        Object stringVerifier = null;
        if (supportsParameter(parameterContext, extensionContext)) {
            String baselineName = getBaselineName(extensionContext);
            ApprovalTestingConfiguration approvalTestingConfiguration =
                    new ApprovalTestingConfiguration();
            BaselineRepositoryImpl baselineRepository =
                    new BaselineRepositoryImpl(approvalTestingConfiguration);
            ApprovalTestingEngine approvalTestingEngine =
                    new ApprovalTestingEngine(baselineRepository, approvalTestingConfiguration,
                            baselineName);
            stringVerifier = approvalTestingEngine.getStringVerifier();
        }
        return stringVerifier;
    }
}
