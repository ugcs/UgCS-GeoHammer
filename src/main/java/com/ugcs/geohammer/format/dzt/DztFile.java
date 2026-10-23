package com.ugcs.geohammer.format.dzt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ugcs.geohammer.format.Channel;
import com.ugcs.geohammer.format.MultiChannelFile;
import com.ugcs.geohammer.format.TraceFile;
import com.ugcs.geohammer.format.gpr.Trace;
import com.ugcs.geohammer.format.meta.Meta;
import com.ugcs.geohammer.model.IndexRange;
import com.ugcs.geohammer.model.element.BaseObject;
import com.ugcs.geohammer.util.AuxElements;
import com.ugcs.geohammer.util.Check;
import com.ugcs.geohammer.util.FileNames;
import org.jspecify.annotations.Nullable;

public class DztFile extends TraceFile implements MultiChannelFile {

	@Nullable
	private File sourceFile;

	private DzgFile dzg = new DzgFile();

	private List<DztChannel> channels = new ArrayList<>();

	private int selectedChannelIndex = 0;

	private DztChannel selectedChannel() {
		return channels.get(selectedChannelIndex);
	}

	@Override
	public double getSampleIntervalNanos() {
		DztHeader header = selectedChannel().getHeader();
		if (header.rhf_range <= 0 || header.rh_nsamp <= 0) {
			return 0;
		}
		return (double) header.rhf_range / header.rh_nsamp;
	}

	@Override
	public int getSelectedChannelIndex() {
		return selectedChannelIndex;
	}

	@Override
	public void selectChannel(int channelIndex) throws IOException {
		if (channelIndex < 0 || channelIndex >= channels.size()) {
			throw new IllegalArgumentException("Channel index out of range: " + channelIndex);
		}
		selectedChannelIndex = channelIndex;

		DztChannel channel = selectedChannel();
		IndexRange sampleRange = meta != null ? meta.getSampleRange() : null;
		setTraces(channel.getTraces());
		updateTraces();
		if (meta == null) {
			loadMeta();
		} else {
			syncMeta();
		}
		if (sampleRange != null && meta != null) {
			meta.setSampleRange(sampleRange);
			syncMeta();
		}

		copyMarkedTracesToAuxElements();
		updateTraceDistances();
	}

	@Override
	public void syncMeta() {
		super.syncMeta();

		if (meta == null) {
			return;
		}

		// meta sample range applies to all channels, not only the selected one
		IndexRange sampleRange = meta.getSampleRange();
		for (DztChannel channel : channels) {
			for (Trace trace : channel.getTraces()) {
				trace.setSampleRange(sampleRange);
			}
		}
	}

	@Override
	public int numChannels() {
		return channels.size();
	}

	@Override
	public Channel getChannel(int channelIndex) {
		if (channelIndex < 0 || channelIndex >= channels.size()) {
			throw new IllegalArgumentException("Channel index out of range: " + channelIndex);
		}
		return channels.get(channelIndex);
	}

	@Override
	public void open(File file) throws IOException {
		Check.notNull(file);

		setFile(file);
		this.sourceFile = file;

		dzg.load(getDzgFile(file));

		try (DztReader r = new DztReader(file, dzg)) {
			channels = r.read();
		}

		selectChannel(0);
		setUnsaved(false);
	}

	@Override
	public void save(File file) throws IOException {
		save(file, new IndexRange(0, numTraces()));
	}

	@Override
	public void save(File file, IndexRange range) throws IOException {
		Check.notNull(file);
		Check.notNull(range);
		Check.notNull(sourceFile);

		// range is in meta space;
		// the writer and dzg take source file trace indices
		int[] fileTraceIndices = new int[range.size()];
		for (int i = 0; i < fileTraceIndices.length; i++) {
			fileTraceIndices[i] = getFileTraceIndex(range.from() + i);
		}

		try (DztWriter w = new DztWriter(file)) {
			w.write(channels, fileTraceIndices);
			dzg.save(getDzgFile(file), buildDzgMappings(fileTraceIndices));
		}
	}

	private File getDzgFile(File file) {
		String path = FileNames.removeExtension(file.getAbsolutePath())
				+ ".dzg";
		return new File(path);
	}

	private List<DzgFile.IndexMapping> buildDzgMappings(int[] fileTraceIndices) {
		List<DzgFile.IndexMapping> mappings = new ArrayList<>();
		for (int i = 0; i < fileTraceIndices.length; i++) {
			if (dzg.hasIndex(fileTraceIndices[i])) {
				mappings.add(new DzgFile.IndexMapping(i, fileTraceIndices[i]));
			}
		}
		return mappings;
	}

	@Override
	public DztFile copy() {
		// ground profile is not copied
		DztFile copy = new DztFile();
		copy.sourceFile = this.sourceFile;
		copy.dzg = this.dzg;
		copy.selectedChannelIndex = this.selectedChannelIndex;

		copy.setFile(getFile());
		copy.setUnsaved(isUnsaved());

		copy.channels = new ArrayList<>(this.channels.size());
		for (DztChannel src : this.channels) {
			copy.channels.add(src.copy());
		}

		// Invariant: DztFile.traces and selectedChannel.getTraces() are the
		// same list reference. Take it from the freshly copied channel.
		List<Trace> tracesCopy = copy.channels.get(copy.selectedChannelIndex).getTraces();
		copy.setTraces(tracesCopy);

		List<BaseObject> elementsCopy = AuxElements.copy(getAuxElements());
		copy.setAuxElements(elementsCopy);

		copy.meta = Meta.copy(meta);

		return copy;
	}
}
