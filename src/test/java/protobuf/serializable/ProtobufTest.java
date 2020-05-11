/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package protobuf.serializable;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import protobuf.serializable.annotation.CollectionType;
import protobuf.serializable.annotation.MapType;
import protobuf.serializable.annotation.TagValue;

public class ProtobufTest {
    static class SubClass {
        String sub;
        @MapType(keyType = String.class, valueType = Integer.class)
        Map<String, Integer> map;
    }
    static class MyClass {
        String name;
        int value;
        @TagValue(6)
        float fot;
        @CollectionType(String.class)
        List<String> stringList;
        SubClass sub;
    }

    @Test
    public void outputMethod() throws IOException, InstantiationException, IllegalAccessException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ProtobufOutput protobufOutput = new ProtobufOutput(byteArrayOutputStream);

        MyClass my = new MyClass();
        my.name = "My class";
        my.value = 101;
        my.fot = 11.3f;
        my.stringList = new ArrayList<>();
        my.stringList.add("first");
        my.stringList.add("second");
        my.sub=new SubClass();
        my.sub.sub="SubString";
        my.sub.map=new TreeMap<>();
        my.sub.map.put("k1", 1);
        my.sub.map.put("k2", 2);
        my.sub.map.put("k3", 3);

        protobufOutput.write(2, my);
        protobufOutput.close();

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);

        ProtobufInput protobufInput = new ProtobufInput(byteArrayInputStream);
        MyClass you = protobufInput.readClass(2, MyClass.class);
        Assert.assertEquals(my.name, you.name);
        Assert.assertEquals(my.value, you.value);
        Assert.assertTrue(Math.abs(my.fot - you.fot) < 0.0001);
        Assert.assertArrayEquals(my.stringList.toArray(), you.stringList.toArray());

        protobufInput.close();

    }
}