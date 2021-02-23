package com.function.karaoke.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    public static Song parse(List<String> data) {
        Song song = new Song();
        Song.Line lastLine = null;
        int j = -1;
        for (int i = 0; i < data.size() - 1; i++) {
            String line = data.get(i);


            if (line.startsWith("#MP3")) {
                song.file = getStringValue(line, "#MP3");
            } else if (line.startsWith("#COVER")) {
                song.cover = getStringValue(line, "#COVER");
            } else {

//        for (String line : data) {
                if (line.length() == 0) {
                    continue;
                }
                // tags

                if (line.startsWith("[ti")) {
                    song.title = getStringValueOfLine(line);
                } else if (line.startsWith("[ar")) {
                    song.artist = getStringValueOfLine(line);
                } else if (line.startsWith("[al")) {
                    song.album = getStringValueOfLine(line);
                } else if (line.startsWith("[") && line.endsWith("]")) {
                } else {
                    if (line.startsWith("[")) {
                        int nextLineIndex = i + 1;
                        boolean isLastLine = false;
                        String nextLine = "";
                        while (nextLine.equals("")) {
                            if (nextLineIndex == data.size()) {
                                isLastLine = true;
                                break;
                            }
                            nextLine = data.get(nextLineIndex);
                            nextLineIndex++;
                        }
                        String[] lineWordsAndTimes = line.split("<");

                        if ((lineContainsDollar(lineWordsAndTimes) && lineWordsAndTimes.length > 5) || (!lineContainsDollar(lineWordsAndTimes) && lineWordsAndTimes.length > 4)
                                || (lineContainsDollar(lineWordsAndTimes) && lengthOfLineIsGreaterThan(lineWordsAndTimes, 55)) ||
                                (!lineContainsDollar(lineWordsAndTimes) && lengthOfLineIsGreaterThan(lineWordsAndTimes, 48))) {
                            List<String[]> lines = breakLineIntoManyLines(lineWordsAndTimes);
                            for (int k = 0; k < 2; k++) {
                                String[] l = lines.get(k);

                                Song.Line nextLineInSong = parseLine(l, getLineTimeStamp(k == 0 ? lines.get(1)[0] : nextLine), isLastLine);
                                song.lines.add(nextLineInSong);
                                if (lastWordIsUnproportionatelyLong(getLineTimeStamp(k == 0 ? lines.get(1)[0] : nextLine), nextLineInSong.to)) {
                                    song.lines.add(addIntroIndication(getLineTimeStamp(k == 0 ? lines.get(1)[0] : nextLine), false));
                                }
                            }
                        } else {
                            Song.Line nextLineInSong = parseLine(lineWordsAndTimes, getLineTimeStamp(nextLine), isLastLine);
                            song.lines.add(nextLineInSong);
                            if (lastWordIsUnproportionatelyLong(getLineTimeStamp(nextLine), nextLineInSong.to)) {
                                song.lines.add(addIntroIndication(getLineTimeStamp(nextLine), false));
                            }
                        }
                    } else {
                        if (i < data.size() - 1) {
                            song.lines.add(parseHebrewLine(line.split(">"), data.get(i + 1), false));
                        } else {
                            song.lines.add(parseHebrewLine(line.split(">"), data.get(i + 1), true));
                        }
                    }
                }
            }
        }

        // fix starts
        for (Song.Line line : song.lines)
            if (line.from == 0 && line.syllables.size() > 0)
                line.from = line.syllables.get(0).from;
        song.lines.add(0, addIntroIndication(song.lines.get(0).from, true));

        return song;
    }

    private static boolean lengthOfLineIsGreaterThan(String[] lineWordsAndTimes, int i) {
        int counter = 0;
        for (String word : lineWordsAndTimes)
            counter += word.length();
        return counter > i;
    }

    private static boolean lineContainsDollar(String[] lineWordsAndTimes) {
        return lineWordsAndTimes[lineWordsAndTimes.length - 1].contains("$");
    }

    private static List<String[]> breakLineIntoManyLines(String[] lineWordsAndTimes) {
        List<String[]> allLines = new ArrayList<>();
        int lengthOfNewSentence = (lineWordsAndTimes.length - 1) > 6 ? 4 : lineWordsAndTimes.length < 5 ? 2 : 3;

        for (int k = 0; k < 2; k++)
            allLines.add(parsePartOfLine(lineWordsAndTimes, lengthOfNewSentence, k * lengthOfNewSentence));
        return allLines;
    }

    private static String[] parsePartOfLine(String[] lineWordsAndTimes, int lengthOfNewSentence, int i) {
        int lengthOfSentence = i == 0 ? lengthOfNewSentence + 1 : lineWordsAndTimes.length - lengthOfNewSentence;
        String[] nextLine = new String[lengthOfSentence];
        for (int j = 0; j < lengthOfSentence - 1; j++) {
            if (i + j > lineWordsAndTimes.length - 2)
                break;
            if (j == 0) {
                if (lineWordsAndTimes[i].contains("]")) {
                    nextLine[0] = lineWordsAndTimes[0];
                } else {
                    nextLine[0] = addFrontBracketToSentence(lineWordsAndTimes[i]);
                }
            } else
                nextLine[j] = lineWordsAndTimes[i + j];
        }
        i = Math.min(i + lengthOfSentence - 1, lineWordsAndTimes.length - 1);
        if (lineContainsDollar(lineWordsAndTimes))
            if (lineWordsAndTimes[i].contains("$"))
                nextLine[lengthOfSentence - 1] = lineWordsAndTimes[i];
            else
                nextLine[lengthOfSentence - 1] = swapWordForDollar(lineWordsAndTimes[i]);
        else {
            if (i == lineWordsAndTimes.length - 1)
                nextLine[lengthOfSentence - 1] = lineWordsAndTimes[i];
            else
                nextLine[lengthOfSentence - 1] = swapWordForDollar(lineWordsAndTimes[i - 1]);
        }


        return nextLine;
    }

    private static String addFrontBracketToSentence(String lineWordsAndTime) {
        StringBuffer stringBuffer = new StringBuffer(lineWordsAndTime.replace('>', ']'));
        stringBuffer.insert(0, "[");
        return String.valueOf(stringBuffer);
    }

    private static String swapWordForDollar(String lineWordsAndTime) {
        String tempWord = lineWordsAndTime;
        String[] wordAndTime = tempWord.split(">");
        return tempWord.replace(wordAndTime[1], "$");

    }


    private static boolean lastWordIsUnproportionatelyLong(double parseTimeStamp, double to) {
        return parseTimeStamp != to;
    }

    private static Song.Line addIntroIndication(double from, boolean beginning) {
        double nextSyllableStartsAt = from;
        Song.Line indicatorLine = new Song.Line();
        indicatorLine.from = beginning ? 0 : from - 3;
        indicatorLine.to = from;
        Song.Syllable syllable;
        int introSeconds = 3;
        for (int i = 0; i < introSeconds; i++) {
            syllable = new Song.Syllable();
            syllable.text = getIndexIndicators(i + 1) + " ";
            syllable.to = nextSyllableStartsAt;
            syllable.from = syllable.to - 1;
            nextSyllableStartsAt = syllable.from;
            syllable.letters = addLettersToSyllable(syllable);
            indicatorLine.syllables.add(0, syllable);
        }
        return indicatorLine;
    }

    private static String getIndexIndicators(int i) {
        char[] chars = new char[i];
        Arrays.fill(chars, '·');
        return new String(chars);
    }

    private static Song.Line parseHebrewLine(String[] line, String nextLine, boolean lastLine) {
        Song.Line currentLine = new Song.Line();
        Song.Syllable syllable;
        double startTime = 0;
        double endTime = 0;
        for (int i = 0; i < line.length; i++) {
            syllable = new Song.Syllable();
            String word = line[i];
            if (i == line.length - 1) {
                startTime = getLineTimeStamp(word);
                currentLine.from = startTime;
                syllable.text = word.substring(word.indexOf(0, word.indexOf("[")));
            } else {
                String[] wordAndTime = word.split("<");
                if (i > 0) {
                    startTime = parseTimeStamp(wordAndTime[1]);
                }
                syllable.text = wordAndTime[0];
            }
            if (i == 0) {
                if (lastLine) {
                    endTime = startTime + 100000;
                } else {
                    endTime = getLineTimeStamp(nextLine);
                }
            }
            syllable.from = startTime;
            syllable.to = endTime;
            syllable.letters = addLettersToSyllable(syllable);
            currentLine.syllables.add(syllable);
            endTime = startTime;
        }
        currentLine.to = endTime;
        return currentLine;
    }

    private static Song.Line parseLine(String[] line, double nextLineTimeStamp, boolean lastLine) {
        Song.Line currentLine = new Song.Line();
        Song.Syllable syllable;
        double startTime = 0;
        double endTime = 0;
        for (int i = 0; i < line.length; i++) {
            syllable = new Song.Syllable();
            String word = line[i];
            if (word.startsWith("[")) {
                startTime = getLineTimeStamp(word);
                currentLine.from = startTime;
                for (int j = 0; j < word.length(); j++) {
                    char n = word.charAt(j);
                    if (n == ']') {
                        syllable.text = word.substring(j + 1);
                        break;
                    }
                }
            } else {
                String[] wordAndTime = word.split(">");
                if (i > 0) {
                    startTime = parseTimeStamp(wordAndTime[0]);
                }
                if (wordAndTime[1].contains("$")) {
                    endTime = nextLineTimeStamp;
                    if (endTime - startTime > 5) {
                        endTime = startTime;
                    }
                    break;
                }
                syllable.text = wordAndTime[1];
            }
            if (i < line.length - 1) {
                endTime = parseTimeStamp(line[i + 1].split(">")[0]);

            } else {
                if (lastLine) {
                    endTime = startTime + 100000;
                } else {
                    endTime = nextLineTimeStamp;
                }
                if (endTime - startTime > 5) {
                    endTime = startTime + 2;
                }
            }
            syllable.from = startTime;
            syllable.to = endTime;

            syllable.letters = addLettersToSyllable(syllable);
            currentLine.syllables.add(syllable);
            startTime = endTime;
        }
        currentLine.to = endTime;
        return currentLine;
    }

    private static List<Song.Letter> addLettersToSyllable(Song.Syllable syllable) {
        double totalTimeAllotted = syllable.to - syllable.from;
        double lengthPerLetter = totalTimeAllotted / (double) syllable.text.length();
        double currentPosition = syllable.from;
        List<Song.Letter> letters = new ArrayList<>();
        for (char letter : syllable.text.toCharArray()) {
            Song.Letter l = new Song.Letter();
            l.from = currentPosition;
            l.to = currentPosition + lengthPerLetter;
            currentPosition = l.to;
            l.letter = letter;
            letters.add(l);
        }
        return letters;
    }

    private static double getLineTimeStamp(String line) {
        if (line.contains("[")) {
            return parseTimeStamp(line.substring(line.indexOf("[") + 1, line.indexOf("]")));
        }
        return 0;
    }

    private static double parseTimeStamp(String timeStamp) {
        String[] minutesSecondsHundredthSeconds = timeStamp.split(":");
        int seconds = Integer.parseInt(timeStamp.substring(0, 2)) * 60 +
                Integer.parseInt(timeStamp.substring(3, 5));
        int hundredthOfSeconds = Integer.parseInt(timeStamp.substring(6, 8));
        return seconds + (double) hundredthOfSeconds / (double) 100;
    }

    private static double getTimestamp(Song song, int beat) {
        return song.gap + beat * 60 / song.BPM;
    }

    private static int[] parseInts(String line) {
        String[] parts = line.substring(2).split(" ");
        int[] res = new int[parts.length];
        for (int p = 0; parts.length != p; ++p)
            res[p] = Integer.parseInt(parts[p]);
        return res;
    }

    private static Song.Syllable parseSyllable(Song song, String line) {
        Song.Syllable s = new Song.Syllable();
        String[] split = line.split(" ", 5);
        String type = split[0];
        int pos = Integer.parseInt(split[1]);
        int len = Integer.parseInt(split[2]);
        s.tone = Integer.parseInt(split[3]);
        if (split.length > 4)
            s.text = split[4];
        else
            s.text = "~";
        s.from = getTimestamp(song, pos);
        s.to = s.from + len * 60 / song.BPM;
        return s;
    }

//    private static float getFloatValue(String line, String tag) {
//        String valueStr = getStringValue(line, tag).replace(',', '.');
//        return Float.valueOf(valueStr);
//    }

    private static String getStringValueOfLine(String line) {
        return line.substring(1, line.length() - 1).split(":")[1];
    }

    private static String getStringValue(String line, String tag) {
        return line.substring(tag.length() + 1);
    }
}
