package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class OOPUnitCore
{
    private static ArrayList<Field> getFields(Object obj) {
        ArrayList<Field> obj_fields = new ArrayList<>();
        Class<?> curr_class = obj.getClass();
        while (curr_class != Object.class) {
            obj_fields.addAll(Arrays.asList(curr_class.getDeclaredFields()));
            curr_class = curr_class.getSuperclass();
        }

        return obj_fields;
    }

    private static int get_method_index(Method m, ArrayList<Method> methods_array) {
        // TODO: check if it is needed to also add parameter types check, including to getName
        return methods_array.
                stream().
                map(x -> x.getName()).
                collect(Collectors.toList()).
                indexOf(m.getName());
    }

    private static void invoke_methods(Object new_test_class, ArrayList<Method> methods_array) {
        for (Method m : methods_array) {
            m.setAccessible(true);
            try {
                m.invoke(new_test_class);
            } catch (Exception e) {
                // should not reach here - setup failed
                throw new OOPAssertionFailure();
            }
        }
    }

    public static void disableFinalPrivate(Field field) throws Exception {
        // first, disable the 'private' modifier (this is not a hack - it's
        // what setAccessible is designed to do)
        field.setAccessible(true);
        // all modifiers are stored in one int, acting as a bitmap
        int fieldModifiers = field.getModifiers();
        if (Modifier.isFinal(fieldModifiers)) {
            // the field is final, as its 'modifiers' member indicates.
            // we'd like to change that member, but it's private.
            Field fieldMod = Field.class.getDeclaredField("modifiers");
            // so now we disable the private limitation
            fieldMod.setAccessible(true);
            // and turn the FINAL bit off. Note that the object whose
            // 'modifiers' member is being modified is the original
            // field, which describes Integer$IntegerCache.cache
            fieldMod.setInt(field, fieldModifiers & ~Modifier.FINAL);
            // So now we've used reflection to work around its own limitations,
            // and the final modifier has been disabled...
            // this is sufficient, because when modifying a final field
            // using reflection, the exception is thrown by the reflection
            // code and not by some lower-level JVM stuff.
        }
    }

    private static ArrayList<Method> get_all_setup_methods(Object new_test_class) {
        Class<?> curr_class = new_test_class.getClass();
        ArrayList<Method> setup_methods = new ArrayList<>();
        while (curr_class != Object.class) {
            Method[] curr_methods = curr_class.getDeclaredMethods();
            for (Method m : curr_methods) {
                if (m.getDeclaredAnnotation(OOPSetup.class) == null) {
                    // m was not defined with the annotation
                    continue;
                }
                // were iterating from child to parent. Must Check if child contains any newer implementation of the method.
                // We figure this out only when parent is reached
                int method_index = get_method_index(m, setup_methods);
                if (method_index > -1) {
                    Method child_impl_m = setup_methods.get(method_index);
                    setup_methods.remove(child_impl_m); // delete from index in the middle
                    setup_methods.add(child_impl_m);    // add to the end of the array (instead of the current m)
                } else {
                    setup_methods.add(m);
                }
            }
            curr_class = curr_class.getSuperclass();
        }
        setup_methods.removeAll(Collections.singleton(null));  // remove all "holes"
        Collections.reverse(setup_methods);

        return setup_methods;
    }

    private static ArrayList<Method> get_all_before_methods(Object new_test_class) {
        Class<?> curr_class = new_test_class.getClass();
        ArrayList<Method> before_methods = new ArrayList<>();
        while (curr_class != Object.class) {
            Method[] curr_methods = curr_class.getDeclaredMethods();
            for (Method m : curr_methods) {
                if (m.getDeclaredAnnotation(OOPBefore.class) == null) {
                    // m was not defined with the annotation
                    continue;
                }
                // were iterating from child to parent. Must Check if child contains any newer implementation of the method.
                // We figure this out only when parent is reached
                int method_index = get_method_index(m, before_methods);
                if (method_index > -1) {
                    Method child_impl_m = before_methods.get(method_index);
                    before_methods.remove(child_impl_m); // delete from index in the middle
                    before_methods.add(child_impl_m);    // add to the end of the array (instead of the current m)
                } else {
                    before_methods.add(m);
                }
            }
            curr_class = curr_class.getSuperclass();
        }
        before_methods.removeAll(Collections.singleton(null));  // TODO: make sure it removes all nulls
        Collections.reverse(before_methods);

        return before_methods;
    }

    private static ArrayList<Method> get_all_after_methods(Object new_test_class) {
        Class<?> curr_class = new_test_class.getClass();
        ArrayList<Method> after_methods = new ArrayList<>();
        while (curr_class != Object.class) {
            Method[] curr_methods = curr_class.getDeclaredMethods();
            for (Method m : curr_methods) {
                if (m.getDeclaredAnnotation(OOPAfter.class) == null) {
                    // m was not defined with the annotation
                    continue;
                }
                // were iterating from child to parent. Must Check if child contains any newer implementation of the method.
                // We figure this out only when parent is reached
                int method_index = get_method_index(m, after_methods);
                if (method_index > -1) {
                    Method child_impl_m = after_methods.get(method_index);
                    after_methods.remove(child_impl_m); // delete from index in the middle
                    after_methods.add(child_impl_m);    // add to the end of the array (instead of the current m)
                } else {
                    after_methods.add(m);
                }
            }
            curr_class = curr_class.getSuperclass();
        }
        after_methods.removeAll(Collections.singleton(null));  // TODO: make sure it removes all nulls

        return after_methods;
    }

    private static ArrayList<Method> get_all_test_methods_unordered(Object new_test_class) {
        Class<?> curr_class = new_test_class.getClass();
        ArrayList<Method> test_methods = new ArrayList<>();
        while (curr_class != Object.class) {
            Method[] curr_methods = curr_class.getDeclaredMethods();
            for (Method m : curr_methods) {
                if (m.getDeclaredAnnotation(OOPTest.class) == null) {
                    // m was not defined with the annotation
                    continue;
                }
                // were iterating from child to parent. Must Check if child contains any newer implementation of the method.
                // We figure this out only when parent is reached
                int method_index = get_method_index(m, test_methods);
                if (method_index > -1) {
                    Method child_impl_m = test_methods.get(method_index);
                    test_methods.remove(child_impl_m); // delete from index in the middle
                    test_methods.add(child_impl_m);    // add to the end of the array (instead of the current m)
                } else {
                    test_methods.add(m);
                }
            }
            curr_class = curr_class.getSuperclass();
        }
        test_methods.removeAll(Collections.singleton(null));  // remove all "holes"

        return test_methods;
    }

    private static ArrayList<Method> get_all_test_methods_ordered(Object new_test_class)
    {
        ArrayList<Method> test_methods = get_all_test_methods_unordered(new_test_class);
        test_methods.sort(Comparator.comparingInt(x -> x.getDeclaredAnnotation(OOPTest.class).order()));

        return test_methods;
    }

    private static ArrayList<Method> get_all_tagged_methods(ArrayList<Method> test_methods, String tag)
    {
        ArrayList<Method> tagged_methods = new ArrayList<>();
        for (Method m : test_methods)
        {
            if (m.getDeclaredAnnotation(OOPTest.class).tag().equals(tag))
            {
                tagged_methods.add(m);
            }
        }

        return tagged_methods;
    }

    private static ArrayList<Method> get_all_untagged_methods(ArrayList<Method> test_methods, String tag)
    {
        ArrayList<Method> untagged_methods = new ArrayList<>();
        for (Method m : test_methods)
        {
            if (!m.getDeclaredAnnotation(OOPTest.class).tag().equals(tag))
            {
                untagged_methods.add(m);
            }
        }

        return untagged_methods;
    }

    private static ArrayList<Method> get_all_test_methods(Object new_test_class)
    {
        ArrayList<Method> test_methods = new ArrayList<>();
        if (new_test_class.getClass().getAnnotation(OOPTestClass.class).value() == OOPTestClass.OOPTestClassType.ORDERED)
        {
            test_methods = get_all_test_methods_ordered(new_test_class);
        }
        else
        {
            test_methods = get_all_test_methods_unordered(new_test_class);
        }

        return test_methods;
    }

    private static void activate_setup_methods(Object new_test_class) {
        ArrayList<Method> setup_methods = get_all_setup_methods(new_test_class);
        // Note - reversing the list here
        // In case child overrides a method of parent, we indeed call the child method, instead of the parent method.

        invoke_methods(new_test_class, setup_methods);
    }

    private static Object get_expected_exception(Object new_test_class) {
        Class<?> curr_class = new_test_class.getClass();
        while (curr_class != Object.class) {
            Field[] curr_fields = curr_class.getDeclaredFields();
            for (Field f : curr_fields) {
                if (f.getDeclaredAnnotation(OOPExceptionRule.class) == null) {
                    // f was not defined with the annotation
                    continue;
                }
                f.setAccessible(true);

                try {
                    return f.get(new_test_class);
                } catch (Exception e) {
                    // we should not get in here
                    throw new IllegalArgumentException();
                }
            }
            curr_class = curr_class.getSuperclass();
        }

        return null;
    }

    public static void assertEquals(Object expected, Object actual) {
        // same address (exactly same object)
        if (expected == actual) {
            return;
        }

        // one is null, other isnt. OR different classes
        if (expected == null || actual == null || expected.getClass() != actual.getClass()) {
            throw new OOPAssertionFailure();
        }


        ArrayList<Field> expected_fields = getFields(expected);

        for (Field field : expected_fields) {
            try {
                field.setAccessible(true);
                Object expected_val = field.get(expected);
                Object actual_val = field.get(actual);
                if ((expected_val == null && actual_val != null) || ((expected_val != null) && (!expected_val.equals(actual_val)))) {
                    throw new OOPAssertionFailure();
                }
            } catch (IllegalAccessException e) {
                // should not reach here
                throw new OOPAssertionFailure();
            }
        }
    }

    public static void fail() {
        throw new OOPAssertionFailure();
    }

    private static Object generate_field_backup(Object value_to_backup)
    {
        Object backup_object = new Object();
        if (value_to_backup instanceof Cloneable)
        {
            try
            {
                Method clone_method = value_to_backup.getClass().getDeclaredMethod("clone");
                clone_method.setAccessible(true);
                backup_object = clone_method.invoke(value_to_backup);
                return backup_object;
            }
            catch (Exception e)
            {
                try
                {
                    // does not catch private cloners up in the tree?
                    Method clone_method = value_to_backup.getClass().getMethod("clone");
                    clone_method.setAccessible(true);
                    backup_object = clone_method.invoke(value_to_backup);
                    return backup_object;
                }
                catch(Exception e_inner)
                {
                    // should not reach here
                }
            }
        }

        else
        {
            Constructor[] constructors = value_to_backup.getClass().getDeclaredConstructors();
            for (Constructor c : constructors)
            {
                if ((c.getParameterCount() == 1) && (c.getParameterTypes()[0].equals(value_to_backup.getClass())))
                {
                    c.setAccessible(true);
                    try
                    {
                        backup_object = c.newInstance(value_to_backup);
                        return backup_object;
                    }
                    catch (Exception e)
                    {
                        // should not reach here
                    }
                }
            }
        }

        // no clone, nor copy-ctor. return the field itself as a lame backup
        return value_to_backup;
    }

    private static Object create_backup(Object obj)
    {
        Object backup_obj;
        try
        {
            Constructor<?> obj_ctor = obj.getClass().getDeclaredConstructor();
            obj_ctor.setAccessible(true);
            backup_obj = obj_ctor.newInstance();
        }
        catch (Exception e)
        {
            // should not reach here - original class doesnt contain default ctor
            throw new IllegalArgumentException();
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields)
        {
            try {
                f.setAccessible(true);
                Object field_value = f.get(obj);
                Object field_backup = generate_field_backup(field_value);
                f.set(backup_obj, field_backup);
            }
            catch (Exception e)
            {
                // bad stuff happened
            }
        }

        return backup_obj;
    }

    private static void run_after_methods(Method m, ArrayList<Method> after_methods, Object new_test_class) throws InvocationTargetException, IllegalAccessException
    {
        // Run after methods
        for (Method a : after_methods)
        {
            if (Arrays.stream(a.getDeclaredAnnotation(OOPAfter.class).value()).anyMatch(x -> x.equals(m.getName())))
            {
                a.invoke(new_test_class);
            }
        }
    }

    private static void run_before_methods(Method m, ArrayList<Method> before_methods, Object new_test_class) throws InvocationTargetException, IllegalAccessException
    {
        // Run before methods
        for (Method b : before_methods)
        {
            // Check if m is one of the methods defined with 'b' as its before method
            if (Arrays.stream(b.getDeclaredAnnotation(OOPBefore.class).value()).anyMatch(x -> x.equals(m.getName())))
            {
                b.invoke(new_test_class);
            }
        }
    }

    public static OOPResult get_oop_result(OOPExpectedException expected_exception, InvocationTargetException e)
    {
        OOPResult result;
        try
        {
            result = new OOPResultImpl(expected_exception, (Exception) e.getTargetException());
        }
        catch (Exception e_new)
        {
            result = new OOPResultImpl(expected_exception, (OOPAssertionFailure) e.getTargetException());
        }

        return result;
    }

    private static OOPResult invoke_method(Method m, Object new_test_class, HashMap<String, OOPResult> tests_map)
    {
        try
        {
            m.invoke(new_test_class);
            // TODO: need to reset the expected exception before every test
            OOPExpectedException expected_exception = (OOPExpectedExceptionImpl) get_expected_exception(new_test_class);
            return new OOPResultImpl(expected_exception);
        }
        catch (InvocationTargetException e)
        {
            // We got exception from running the test. Check if its "regular" exception, or OOPAssertionFailure
            OOPExpectedException expected_exception = (OOPExpectedExceptionImpl) get_expected_exception(new_test_class);
            return get_oop_result(expected_exception, e);
        }
        catch (Exception e)
        {
            // should not get in here
            throw new IllegalArgumentException();
        }
    }

    public static void run_tests(ArrayList<Method> test_methods,
                                 ArrayList<Method> before_methods,
                                 ArrayList<Method> after_methods,
                                 Object new_test_class,
                                 HashMap<String, OOPResult> tests_map)
    {
        OOPExpectedExceptionImpl original_expected_exception = (OOPExpectedExceptionImpl) get_expected_exception(new_test_class);

        for (Method m : test_methods)
        {
            // Make backup
            Object object_backup = create_backup(new_test_class);
            // reset expected exception prior to running test - TODO: test this
            if (original_expected_exception != null)
            {
                original_expected_exception.expect(null);
                original_expected_exception._expected_messages.clear();
            }

            try
            {
                run_before_methods(m, before_methods, new_test_class);
            }
            catch (InvocationTargetException e)
            {
                OOPResult result = get_oop_result(null, e);
                tests_map.put(m.getName(), result);
                // TODO: Test this
                new_test_class = object_backup;
                continue;
            }
            catch (Exception e)
            {
                // should not get in here
            }

            OOPResult method_result = invoke_method(m, new_test_class, tests_map);

            try
            {
                run_after_methods(m, after_methods, new_test_class);
            }
            catch (InvocationTargetException e)
            {
                OOPResult result = get_oop_result(null, e);
                tests_map.put(m.getName(), result);
                // TODO: Test this
                new_test_class = object_backup;
                continue;
            }
            catch (Exception e)
            {
                // should not get in here
            }

            tests_map.put(m.getName(), method_result);
        }
    }

    public static void initialize_and_run_all_tests(Object new_test_class, HashMap<String, OOPResult> tests_map)
    {
        ArrayList<Method> before_methods = get_all_before_methods(new_test_class);
        ArrayList<Method> after_methods = get_all_after_methods(new_test_class);
        ArrayList<Method> test_methods = get_all_test_methods(new_test_class);

        run_tests(test_methods, before_methods, after_methods, new_test_class, tests_map);
    }

    public static void initialize_and_run_all_tests(Object new_test_class, HashMap<String, OOPResult> tests_map, String tag)
    {
        ArrayList<Method> before_methods = get_all_before_methods(new_test_class);
        ArrayList<Method> after_methods = get_all_after_methods(new_test_class);
        ArrayList<Method> test_methods = get_all_test_methods(new_test_class);
        ArrayList<Method> tests_tagged = get_all_tagged_methods(test_methods, tag);

        run_tests(tests_tagged, before_methods, after_methods, new_test_class, tests_map);
    }

    public static OOPTestSummary runClass(Class<?> testClass)
    {
        if (testClass == null || testClass.getAnnotation(OOPTestClass.class) == null)
        {
            throw new IllegalArgumentException();
        }

        Object new_test_class;
        try
        {
            Constructor<?> ctor = testClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            new_test_class = ctor.newInstance();
        }
        catch (Exception e)
        {
            // should not reach here - it was promised there is an existing default ctor
            throw new IllegalArgumentException();
        }

        activate_setup_methods(new_test_class);
        HashMap<String, OOPResult> tests_map = new HashMap<>();
        initialize_and_run_all_tests(new_test_class, tests_map);

        return new OOPTestSummary(tests_map);
    }

    public static OOPTestSummary runClass(Class<?> testClass, String tag)
    {
        if (testClass == null || testClass.getAnnotation(OOPTestClass.class) == null || tag == null)
        {
            throw new IllegalArgumentException();
        }

        Object new_test_class;
        try
        {
            Constructor<?> ctor = testClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            new_test_class = ctor.newInstance();
        }
        catch (Exception e)
        {
            // should not reach here - it was promised there is an existing default ctor
            throw new IllegalArgumentException();
        }

        activate_setup_methods(new_test_class);
        HashMap<String, OOPResult> tests_map = new HashMap<>();

        initialize_and_run_all_tests(new_test_class, tests_map, tag);

        return new OOPTestSummary(tests_map);
    }
}
