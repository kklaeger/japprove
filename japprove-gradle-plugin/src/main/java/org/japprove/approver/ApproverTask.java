package org.japprove.approver;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.japprove.config.ApprovalTestingConfiguration;
import org.japprove.engine.ApprovalTestingEngine;
import org.japprove.repositories.BaselineRepositoryImpl;

/**
 * The task that contains the options to approve one or more baseline candidates.
 */
public class ApproverTask extends DefaultTask {

    @Input
    private String baseline = "";

    @Input
    private boolean approveAll = false;

    @Option(option = "baseline",
            description = "Provides the name of the baseline candidate that should be approved")
    public void setBaseline(String baseline) {
        this.baseline = baseline;
    }

    @Option(option = "all", description = "All unapproved baseline candidates should be approved")
    public void setApproveAll() {
        this.approveAll = true;
    }

    /**
     * Approves all baseline candidates, a specific baseline or starts a batch process depending on
     * the set options.
     */
    @TaskAction
    public void approve() {
        ApprovalTestingConfiguration approvalTestingConfiguration =
                new ApprovalTestingConfiguration();
        BaselineRepositoryImpl baselineRepository =
                new BaselineRepositoryImpl(approvalTestingConfiguration);
        ApprovalTestingEngine approvalTestingEngine =
                new ApprovalTestingEngine(baselineRepository, approvalTestingConfiguration);
        Approver approver = approvalTestingEngine.getApprover();
        if (approveAll) {
            approver.approveAllBaselineCandidates();
        } else if (baseline != "") {
            approver.approveBaselineCandidate(baseline);
        } else {
            approver.startApprovingBatchProcess();
        }
    }

    public String getBaseline() {
        return baseline;
    }

    public boolean isApproveAll() {
        return approveAll;
    }
}
