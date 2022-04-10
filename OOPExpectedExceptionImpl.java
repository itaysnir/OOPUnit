package OOP.Solution;

import OOP.Provided.OOPExpectedException;

import java.util.ArrayList;

public class OOPExpectedExceptionImpl implements OOPExpectedException
{
    public Class<? extends Exception> _expected_exception = null;
    public ArrayList<String> _expected_messages = new ArrayList<String>();


    @Override
    public Class<? extends Exception> getExpectedException()
    {
        return _expected_exception;
    }

    @Override
    public OOPExpectedException expect(Class<? extends Exception> expected)
    {
        _expected_exception = expected;
        return this;
    }

    @Override
    public OOPExpectedException expectMessage(String msg)
    {
        _expected_messages.add(msg);
        return this;
    }

    @Override
    public boolean assertExpected(Exception e)
    {
        // TODO: check this
        if (_expected_exception == null) // expecting no exception to be thrown - althought it is wierd, it SHOULD pass this check.
        {
            return true;
        }

        if (e == null)
        {
            return false;
        }

        if (!_expected_exception.isAssignableFrom(e.getClass()))
        {
            return false;
        }

        String received_message = e.getMessage();
        if (received_message == null)
        {
            return _expected_messages.size() == 0;
        }
        else
        {
            for (String m : _expected_messages)
            {
                if (!received_message.contains(m))
                {
                    return false;
                }
            }
        }

        return true;
    }

    public static OOPExpectedException none()
    {
        return new OOPExpectedExceptionImpl();
    }
}
