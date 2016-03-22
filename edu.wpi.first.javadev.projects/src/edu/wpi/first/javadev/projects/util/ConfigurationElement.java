package edu.wpi.first.javadev.projects.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

/**
 * This provides a wrapper class for an IConfigurationElement
 * 
 * @author Joe Grinstead
 */
@SuppressWarnings("deprecation")
public class ConfigurationElement implements IConfigurationElement {

	protected IConfigurationElement	element;

	public ConfigurationElement(IConfigurationElement element) {
		this.element = element;
	}

	@Override
	public Object createExecutableExtension(String propertyName) throws CoreException {
		return element.createExecutableExtension(propertyName);
	}

	@Override
	public String getAttribute(String name) throws InvalidRegistryObjectException {
		return element.getAttribute(name);
	}

	@Override
	public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
		return element.getAttributeAsIs(name);
	}

	@Override
	public String[] getAttributeNames() throws InvalidRegistryObjectException {
		return element.getAttributeNames();
	}

	@Override
	public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
		return element.getChildren();
	}

	@Override
	public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
		return element.getChildren(name);
	}

	@Override
	public IContributor getContributor() throws InvalidRegistryObjectException {
		return element.getContributor();
	}

	@Override
	public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
		return element.getDeclaringExtension();
	}

	@Override
	public String getName() throws InvalidRegistryObjectException {
		return element.getName();
	}

	@Override
	public String getNamespace() throws InvalidRegistryObjectException {
		return element.getNamespace();
	}

	@Override
	public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
		return element.getNamespaceIdentifier();
	}

	@Override
	public Object getParent() throws InvalidRegistryObjectException {
		return element.getParent();
	}

	@Override
	public String getValue() throws InvalidRegistryObjectException {
		return element.getValue();
	}

	@Override
	public String getValueAsIs() throws InvalidRegistryObjectException {
		return element.getValueAsIs();
	}

	@Override
	public boolean isValid() {
		return element.isValid();
	}

	@Override
	public String getAttribute(String attrName, String locale)
			throws InvalidRegistryObjectException {
		return element.getAttribute(attrName, locale);
	}

	@Override
	public String getValue(String locale) throws InvalidRegistryObjectException {
		return element.getValue(locale);
	}

}
