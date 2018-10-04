package com.mobiussoftware.iotbroker.coap;

import java.util.Arrays;

public class ByteArrayWrapper 
{
	private byte[] value;
	
	public ByteArrayWrapper(byte [] value)
	{
		this.value=value;
	}

	public byte[] getValue() 
	{
		return value;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
	
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		ByteArrayWrapper other = (ByteArrayWrapper) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		
		return true;
	}		
}
