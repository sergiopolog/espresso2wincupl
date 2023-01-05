package org.recreativospiscis.espresso2wincupl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.recreativospiscis.espresso2wincupl.devices.PALSpecs;

/**
 * Espresso 2 WinCUPL
 */
public class Espresso2Wincupl {

	public static void main(String[] args) throws Exception {
		if (args.length == 1 || args.length == 2) {
			String inputFilename = args[0];
			String outputFilename = args.length == 2 ? args[1] : args[0] + ".pld";

			String deviceName = getDeviceName(inputFilename);
			Class<?> specsClass = Class
					.forName("org.recreativospiscis.espresso2wincupl.devices." + deviceName.toUpperCase() + "Specs");
			PALSpecs palSpecs = (PALSpecs) specsClass.getConstructor().newInstance(new Object[] {});
			byte[] outputFileData = processWincuplFile(inputFilename, palSpecs);
			writeFile(outputFilename, outputFileData);
			System.out.println(
					"Successfully generated a WinCUPL pld file for " + palSpecs + " into file: " + outputFilename);
		} else {
			System.err.println(
					"Invalid number of arguments. Usage: espresso2wincupl.jar <espresso_equations_input_filename> [pld_output_filename]");
			return;
		}
	}

	private static void writeFile(String fileName, byte[] content) throws IOException {
		if (Files.exists(Paths.get(fileName))) {
			throw new IOException("Output file still exist, do not overwrite it: " + fileName);
		}
		Files.write(Paths.get(fileName), content);
	}

	public static byte[] processWincuplFile(String filename, PALSpecs palSpecs) throws Exception {
		StringBuilder wincuplFileContent = new StringBuilder();

		// set header:
		wincuplFileContent.append(createWinCuplHeader(palSpecs, filename));

		// set input pins section:
		wincuplFileContent.append(createInputPinsSection(filename, palSpecs));

		// set input pins section:
		wincuplFileContent.append(createOutputPinsSection(filename));

		// set input pins section:
		wincuplFileContent.append(createEquationsSection(filename));

		return wincuplFileContent.toString().getBytes();
	}

	private static String getDeviceName(String filename) throws IOException {

		String deviceRegex = "^#\\s((?:PAL|GAL).+)$";
		Pattern devicePattern = Pattern.compile(deviceRegex);

		return Files.lines(Paths.get(filename)).filter(o -> o.matches(deviceRegex)).map(o -> {
			Matcher matcher = devicePattern.matcher(o);
			if (matcher.find()) {
				return matcher.group(1);
			} else {
				return null;
			}
		}).findFirst().get();
	}

	private static String createWinCuplHeader(PALSpecs palSpecs, String filename) {
		StringBuilder header = new StringBuilder();

		String file = Paths.get(filename).getFileName().toString();

		header.append("Name ")
				.append(file.substring(0, file.lastIndexOf(".") > 0 ? file.lastIndexOf(".") : file.length()))
				.append(";");
		header.append("\n");

		header.append("PartNo ").append(";");
		header.append("\n");

		header.append("Date ").append(DateTimeFormatter.ofPattern("dd/MM/uuuu").format(LocalDate.now())).append(" ;");
		header.append("\n");

		header.append("Revision ").append(";");
		header.append("\n");

		header.append("Designer ").append(";");
		header.append("\n");

		header.append("Company ").append(";");
		header.append("\n");

		header.append("Assembly ").append(";");
		header.append("\n");

		header.append("Location ").append(";");
		header.append("\n");

		header.append("Device ").append(palSpecs.getDevicePnemonic()).append(" ;");
		header.append("\n");

		header.append("\n");

		return header.toString();
	}

	private static Integer getInputPinsCount(String filename) throws IOException {

		String phaseRegex = "^#\\sphase\\sis\\s(-+)\\s([0-1]+)$";
		Pattern phasePattern = Pattern.compile(phaseRegex);

		String phaseLine = Files.lines(Paths.get(filename)).filter(o -> o.matches(phaseRegex)).findFirst().get();

		Matcher matcher = phasePattern.matcher(phaseLine);
		if (matcher.find()) {
			return matcher.group(1).split("").length;
		} else {
			return 0;
		}
	}

	private static Stream<String> combineMultiLineEquations(Stream<String> input) {

		List<String> newLines = new ArrayList<String>();
		List<String> lines = input.collect(Collectors.toList());

		String outputMultiLineRegex = "^(\\w{1,2}\\d{1,2})\\s=.+[^;]$";
		Pattern outputMultiLinePattern = Pattern.compile(outputMultiLineRegex);

		for (int i = 0; i < lines.size(); i++) {
			Matcher matcher = outputMultiLinePattern.matcher(lines.get(i));
			if (matcher.find()) {
				newLines.add(lines.get(i) + " " + lines.get(i + 1));
				i++;
			} else {
				newLines.add(lines.get(i));
			}
		}

		return newLines.stream();
	}

	private static String createInputPinsSection(String filename, PALSpecs palSpecs) throws Exception {

		StringBuilder inputPinsSection = new StringBuilder();
		int inputCount = 0;

		inputPinsSection.append("/* ********** INPUT PINS ********** */ ");
		inputPinsSection.append("\n");

		List<String> inputPins = Arrays.asList(palSpecs.getLabels_IN()).stream().filter(s -> s != null)
				.collect(Collectors.toList());
		for (String inputPin : inputPins) {
			inputPinsSection.append("PIN ").append(inputPin.replaceAll("\\D", "")).append(" = ").append(inputPin)
					.append(" ;");
			inputPinsSection.append("\n");
			inputCount++;
		}

		// Get actual output pins:
		String outputRegex = "^(\\w{1,2}\\d{1,2})\\s=.+;$";
		Pattern outputPattern = Pattern.compile(outputRegex, Pattern.DOTALL);

		List<String> outputPins = combineMultiLineEquations(Files.lines(Paths.get(filename)))
				.filter(o -> o.matches(outputRegex)).map(o -> {
					Matcher matcher = outputPattern.matcher(o);
					if (matcher.find()) {
						return matcher.group(1);
					} else {
						return null;
					}
				}).collect(Collectors.toList());

		// Add also the io pins that don't acts as outputs:
		List<String> ioPins = Arrays.asList(palSpecs.getLabels_IO()).stream().filter(s -> s != null)
				.collect(Collectors.toList());
		for (String ioPin : ioPins) {
			if (outputPins.stream().filter(o -> "i".concat(o).equals(ioPin)).findAny().isPresent() == false) {
				inputPinsSection.append("PIN ").append(ioPin.replaceAll("\\D", "")).append(" = ")
						.append(ioPin.replaceAll("io", "i")).append(" ;");
				inputPinsSection.append("\n");
				inputCount++;
			}
		}

		int detectedInputCount = getInputPinsCount(filename);
		if (inputCount != detectedInputCount) {
			throw new Exception(
					"Invalid equations file. Detected inputs: " + detectedInputCount + " but have: " + inputCount);
		}

		inputPinsSection.append("\n");
		return inputPinsSection.toString();
	}

	private static Integer getOutputPinsCount(String filename) throws IOException {

		String phaseRegex = "^#\\sphase\\sis\\s(-+)\\s([0-1]+)$";
		Pattern phasePattern = Pattern.compile(phaseRegex);

		String phaseLine = Files.lines(Paths.get(filename)).filter(o -> o.matches(phaseRegex)).findFirst().get();

		Matcher matcher = phasePattern.matcher(phaseLine);
		if (matcher.find()) {
			return matcher.group(2).split("").length / 2; // Divided by 2 because here OE phase is included for each
															// pin, too.
		} else {
			return 0;
		}
	}

	private static String createOutputPinsSection(String filename) throws Exception {

		StringBuilder outputPinsSection = new StringBuilder();
		int outputCount = 0;

		outputPinsSection.append("/* ********** OUTPUT PINS ********* */ ");
		outputPinsSection.append("\n");

		String outputRegex = "^(\\w{1,2}\\d{1,2})\\s=.+;$";
		Pattern outputPattern = Pattern.compile(outputRegex, Pattern.DOTALL);

		List<String> outputPins = combineMultiLineEquations(Files.lines(Paths.get(filename)))
				.filter(o -> o.matches(outputRegex)).map(o -> {
					Matcher matcher = outputPattern.matcher(o);
					if (matcher.find()) {
						return matcher.group(1);
					} else {
						return null;
					}
				}).collect(Collectors.toList());

		List<String> selectedPhase = getOutputPinsSelectedPhase(filename);

		for (int i = 0; i < outputPins.size(); i++) {
			outputPinsSection.append("PIN ").append(outputPins.get(i).replaceAll("\\D", "")).append(" = ")
					.append(selectedPhase.get(i).equals("0") ? "!" : "").append(outputPins.get(i)).append(" ;");
			outputPinsSection.append("\n");
			outputCount++;
		}

		int detectedOutputCount = getOutputPinsCount(filename);
		if (outputCount != detectedOutputCount) {
			throw new Exception(
					"Invalid equations file. Detected outputs: " + detectedOutputCount + " but have: " + outputCount);
		}

		outputPinsSection.append("\n");
		return outputPinsSection.toString();
	}

	private static List<String> getOutputPinsInitialPhase(String filename) throws IOException {

		String phaseRegex = "^#\\sphase\\sis\\s(-+)\\s([0-1]+)$";
		Pattern phasePattern = Pattern.compile(phaseRegex);

		String phaseLine = Files.lines(Paths.get(filename)).filter(o -> o.matches(phaseRegex)).findFirst().get();

		Matcher matcher = phasePattern.matcher(phaseLine);
		if (matcher.find()) {
			return Arrays.asList(matcher.group(2).split(""));
		} else {
			return Collections.emptyList();
		}
	}

	private static List<String> getOutputPinsSelectedPhase(String filename) throws IOException {

		String phasePositiveRegex = "^#\\s(?:ESPRESSO|EXACT)-POS\\((\\d+)\\).+cost\\sis\\sc=(\\d+)\\(\\d+\\).+$";
		Pattern phasePositivePattern = Pattern.compile(phasePositiveRegex);

		List<Integer> positiveCostsLines = Files.lines(Paths.get(filename)).filter(o -> o.matches(phasePositiveRegex))
				.map(o -> {
					Matcher matcher = phasePositivePattern.matcher(o);
					if (matcher.find()) {
						return Integer.parseInt(matcher.group(2));
					} else {
						return -1;
					}
				}).collect(Collectors.toList());

		String phaseNegativeRegex = "^#\\s(?:ESPRESSO|EXACT)-NEG\\((\\d+)\\).+cost\\sis\\sc=(\\d+)\\(\\d+\\).+$";
		Pattern phaseNegativePattern = Pattern.compile(phaseNegativeRegex);

		List<Integer> negativeCostsLines = Files.lines(Paths.get(filename)).filter(o -> o.matches(phaseNegativeRegex))
				.map(o -> {
					Matcher matcher = phaseNegativePattern.matcher(o);
					if (matcher.find()) {
						return Integer.parseInt(matcher.group(2));
					} else {
						return -1;
					}
				}).collect(Collectors.toList());

		List<String> selectedPhase = getOutputPinsInitialPhase(filename);

		for (int i = 0; i < positiveCostsLines.size(); i++) {
			if (negativeCostsLines.get(i) < positiveCostsLines.get(i)) {
				// Invert the initial phase in case NEG has lower cost:
				String initialPhase = selectedPhase.get(i);
				selectedPhase.set(i, initialPhase.equals("0") ? "1" : "0");
			}
		}

		return selectedPhase;
	}

	private static String createEquationsSection(String filename) throws IOException {

		StringBuilder equationsSection = new StringBuilder();

		equationsSection.append("/* ******** LOGIC EQUATIONS ******* */ ");
		equationsSection.append("\n\n");

		String equationRegex = "(?s)^\\w{1,2}\\d{1,2}\\s=\\s.+;$";

		List<String> equations = combineMultiLineEquations(Files.lines(Paths.get(filename)))
				.filter(o -> o.matches(equationRegex)).collect(Collectors.toList());

		for (String equation : equations) {
			equationsSection.append(equation.replaceAll("\\s{2,}", "").replaceAll("(\\(|\\))", "")
					.replaceAll("&", " & ").replaceAll("\\s\\|\\s", "\n\t# ").replaceAll(";", " ;"));
			equationsSection.append("\n\n");
		}

		return equationsSection.toString();
	}

}