package org.daisy.dotify.studio.api;

import javax.xml.stream.Location;

public final class DocumentPosition implements Comparable<DocumentPosition> {
	private final int line;
	private final int column;
	
	public DocumentPosition(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	public static DocumentPosition with(int line, int column) {
		return new DocumentPosition(line, column);
	}

	public static DocumentPosition with(Location l) {
		return DocumentPosition.with(l.getLineNumber(), l.getColumnNumber());
	}
	
	public boolean isBefore(DocumentPosition other) {
		return getLineNumber()<other.getLineNumber() || getLineNumber()==other.getLineNumber() && getColumnNumber()<other.getColumnNumber();
	}
	
	public boolean isBeforeOrEqual(DocumentPosition other) {
		return getLineNumber()<other.getLineNumber() || getLineNumber()==other.getLineNumber() && getColumnNumber()<=other.getColumnNumber();
	}

	public int getLineNumber() {
		return line;
	}

	public int getColumnNumber() {
		return column;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + line;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentPosition other = (DocumentPosition) obj;
		if (column != other.column)
			return false;
		if (line != other.line)
			return false;
		return true;
	}

	@Override
	public int compareTo(DocumentPosition o) {
		if (this.equals(o)) {
			return 0;
		} else if (isBefore(o)) {
			return -1;
		} else {
			return 1;
		}
	}
}