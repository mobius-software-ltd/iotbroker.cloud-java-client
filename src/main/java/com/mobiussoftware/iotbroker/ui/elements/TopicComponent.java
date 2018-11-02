package com.mobiussoftware.iotbroker.ui.elements;

import java.awt.Component;

public class TopicComponent
{
	private final String name;
	private final Component[] components;

	public TopicComponent(String name, Component[] components)
	{
		this.name = name;
		this.components = components;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		TopicComponent other = (TopicComponent) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName()
	{
		return name;
	}

	public Component[] getComponents()
	{
		return components;
	}
}
