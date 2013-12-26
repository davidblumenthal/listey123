/**
 * 
 */
package com.blumenthal.listey;


/**
 * This is the base class for the nodes in the Listey data hierarchy.
 * It allows us to reuse some of the generic methods in the hierarchy.
 * 
 * @author David
 *
 */
public abstract class TimeStampedNode {
	/**
	 * @return the lastUpdate
	 */
	public abstract Long getLastUpdate();

	/**
	 * @return the uniqueId
	 */
	public abstract String getUniqueId();

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeStampedNode other = (TimeStampedNode) obj;
		if (getUniqueId() == null) {
			if (other.getUniqueId() != null)
				return false;
		} else if (!getUniqueId().equals(other.getUniqueId()))
			return false;
		return true;
	}
	
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getUniqueId() == null) ? 0 : getUniqueId().hashCode());
		return result;
	}
}//TimeStampedNode
