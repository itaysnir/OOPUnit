package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;
import OOP.Provided.OOPExceptionMismatchError;

public class OOPResultImpl implements OOPResult
{
    private OOPTestResult test_result;
    private String test_message;

    // Got no exception ctor
    OOPResultImpl(OOPExpectedException _expected)
    {
        if (_expected == null || _expected.getExpectedException() == null)
        {
            test_result = OOPTestResult.SUCCESS;
            test_message = null;
        }
        else
        {
            test_result = OOPTestResult.ERROR;
            test_message = _expected.getExpectedException().getName();
        }
    }

    OOPResultImpl(OOPExpectedException _expected, OOPAssertionFailure _e)
    {
        test_result = OOPTestResult.FAILURE;
        test_message = _e.getMessage();
    }
    // Got exception ctor
    OOPResultImpl(OOPExpectedException _expected, Exception _e)
    {
        if (_expected == null || _expected.getExpectedException() == null)
        {
            // we got an exception when we expect none, error
            test_result = OOPTestResult.ERROR;
            test_message = _e.getClass().getName();
        }
        else if (!_expected.assertExpected(_e) )
        {
            test_result = OOPTestResult.EXPECTED_EXCEPTION_MISMATCH;
            OOPExceptionMismatchError error = new OOPExceptionMismatchError(_expected.getExpectedException(), _e.getClass());
            test_message = error.getMessage();
        }
        else
        {
            // TODO: validate this
            test_result = OOPTestResult.SUCCESS;
            test_message = null;
        }
    }

    @Override
    public OOPTestResult getResultType()
    {
        return test_result;
    }

    @Override
    public String getMessage()
    {
        return test_message;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (this.getClass() != obj.getClass())
        {
            return false;
        }
        else
        {
            return (getResultType().equals(((OOPResultImpl) obj).getResultType())) &&
                    (getMessage().equals(((OOPResultImpl) obj).getMessage()));
        }
    }
}
