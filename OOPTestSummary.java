package OOP.Solution;

import OOP.Provided.OOPResult;

import java.util.Map;
import java.util.stream.Stream;

public class OOPTestSummary
{
    private Map<String, OOPResult> _test_map;

    OOPTestSummary(Map<String, OOPResult> testMap)
    {
        _test_map = testMap;
    }

    public int getNumSuccesses()
    {
        Stream<OOPResult> stream = _test_map.values().stream();
        return (int)stream.
                filter(x -> x.getResultType() == OOPResult.OOPTestResult.SUCCESS).
                count();
    }

    public int getNumFailures()
    {
        Stream<OOPResult> stream = _test_map.values().stream();
        return (int)stream.
                filter(x -> x.getResultType() == OOPResult.OOPTestResult.FAILURE).
                count();
    }

    public int getNumExceptionMismatches()
    {
        Stream<OOPResult> stream = _test_map.values().stream();
        return (int)stream.
                filter(x -> x.getResultType() == OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH).
                count();
    }

    public int getNumErrors()
    {
        Stream<OOPResult> stream = _test_map.values().stream();
        return (int)stream.
                filter(x -> x.getResultType() == OOPResult.OOPTestResult.ERROR).
                count();
    }
}
