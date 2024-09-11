package com.efeichong.jpa.support;

import com.efeichong.exception.JpaException;

import java.io.*;

/**
 * @author lxk
 * @date 2020/9/25
 * @description 当前类是 {@link java.lang.invoke.SerializedLambda } 的一个镜像
 */
@SuppressWarnings("ALL")
public class SerializedLambda implements Serializable {
    private static final long serialVersionUID = 8025925345765570181L;

    private Class<?> capturingClass;
    private String functionalInterfaceClass;
    private String functionalInterfaceMethodName;
    private String functionalInterfaceMethodSignature;
    private String implClass;
    private String implMethodName;
    private String implMethodSignature;
    private int implMethodKind;
    private String instantiatedMethodType;
    private Object[] capturedArgs;

    public static SerializedLambda extract(Serializable serializable) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(serializable);
            oos.flush();
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    Class<?> clazz = super.resolveClass(desc);
                    return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
                }

            }) {
                return (SerializedLambda) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new JpaException(e);
        }
    }

    public String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    public Class<?> getCapturingClass() {
        return capturingClass;
    }

    public String getImplMethodName() {
        return implMethodName;
    }
}
