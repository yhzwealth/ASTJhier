package edu.fudan.selab.entity.node;

import edu.fudan.selab.utils.SolverUtils;
import lombok.Data;

/**
 * Used to indicate the information of method's parameter or class' field 
 */
@Data
public abstract class AbstractDepNode {
    public boolean isResolved;
    public String type;

    public AbstractDepNode(String type) {
        this.isResolved = false;
        this.type = type;
    }

    public void tryToResolve() {
        assert SolverUtils.isPresentSymbolSolver();
    }
}
