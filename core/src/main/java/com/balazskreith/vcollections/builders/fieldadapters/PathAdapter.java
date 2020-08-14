package com.balazskreith.vcollections.builders.fieldadapters;

import com.balazskreith.vcollections.adapters.Adapter;
import java.nio.file.Path;

public class PathAdapter implements Adapter<String, Path> {

	@Override
	public Path convert(String path) {
		return Path.of(path);
	}

	@Override
	public String deconvert(Path data) {
		return data.toAbsolutePath().toString();
	}
}
