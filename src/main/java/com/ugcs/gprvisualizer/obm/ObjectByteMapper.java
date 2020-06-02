package com.ugcs.gprvisualizer.obm;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.ugcs.gprvisualizer.app.Sout;

public class ObjectByteMapper {


	public void readObject(Object instance, ByteBuffer buffer) throws Exception {
		
		Class<?> clazz = instance.getClass();
		
		for (Field field : clazz.getDeclaredFields()) {
			//Sout.p(field.getName());
			
			if (field.isAnnotationPresent(BaseField.class)) {
				field.setAccessible(true);
				
				
				BaseField annt = field.getAnnotation(BaseField.class);
				
				int position = annt.position();
				buffer.position(position);
				
				int size = annt.size();
				
				//System.out.print(field.getName() + " ( " + position + " ) = ");
				
				Setter ss = map.get(field.getType());
				
				ss.setValue(instance, field, buffer, size);
			}			
		}		
	}
	
	interface Setter {
		void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception;
	}
	
	private static final Map<Class<?>, Setter> map = ImmutableMap.<Class<?>, Setter>builder()
			.put(Short.class, new ShortSetter())
			.put(short.class, new ShortSetter())
			.put(byte.class, new ByteSetter())
			.put(char.class, new CharSetter())
			.put(Float.class, new FloatSetter())
			.put(float.class, new FloatSetter())
			
			.put(byte[].class, new ByteArraySetter())
			.put(String.class, new StringSetter())
			
			.build();
	
	static class ShortSetter implements Setter {

		@Override
		public void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception {
			
			short value = buffer.getShort();
			
			//Sout.p(" READ shot " + value);
			
			field.setShort(instance, value);
		}
	}

	static class ByteSetter implements Setter {

		@Override
		public void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception {
			
			byte value = buffer.get();
			
			//Sout.p(" READ shot " + value);
			
			field.setByte(instance, value);
		}
	}
	
	static class CharSetter implements Setter {

		@Override
		public void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception {
			
			char value = buffer.getChar();
			
			//Sout.p(" READ char " + (int) value);
			
			field.setChar(instance, value);
		}
	}

	static class FloatSetter implements Setter {

		@Override
		public void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception {
			
			float value = buffer.getFloat();
			
			//Sout.p(" READ float " + value);
			
			field.setFloat(instance, value);
			
		}
		
	}

	static class ByteArraySetter implements Setter {

		@Override
		public void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception {
			
			byte[] ba = (byte[]) field.get(instance);
			
			buffer.get(ba);
			
			//Sout.p(" READ bytes " + Arrays.toString(ba));
			
		}
		
	}
	
	static class CharArraySetter implements Setter {

		@Override
		public void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception {
			
			byte[] ba = (byte[]) field.get(instance);
			
			buffer.get(ba);
			
			//Sout.p(" READ bytes " + Arrays.toString(ba));
			
		}		
	}
	
	static class StringSetter implements Setter {

		@Override
		public void setValue(Object instance, Field field, 
				ByteBuffer buffer, int size) throws Exception {
			
			byte[] bytes = new byte[size];
			
			buffer.get(bytes);
			
			String value = new String(bytes);
			Sout.p(" READ string " + value);
			
			field.set(instance, value);
		}
	}

	

}
