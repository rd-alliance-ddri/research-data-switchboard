package org.grants.scopus.response;

public class ScopusObject {
	protected final boolean _fa;
	
	public boolean is_fa() {
		return _fa;
	}
	
	public ScopusObject(boolean _fa) {
		this._fa = _fa;
	}
	
	@Override
	public String toString() {
		return "ScopusObject [_fa=" + _fa + "]";
	}
}
