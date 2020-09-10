package com.function.karaoke.core.model;

import java.util.List;

public class Parser {
    public static Song parse(List<String> data) throws Exception {
        Song song = new Song();
        Song.Line lastLine = null;
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
                    continue;
                } else {
                    if (line.startsWith("[")) {
                        int nextLineIndex = i + 1;
                        boolean isLastLine = false;
                        String nextLine = "";
                        while (nextLine.equals("")){
                            if (nextLineIndex == data.size()){
                                isLastLine = true;
                                break;
                            }
                            nextLine = data.get(nextLineIndex);
                            nextLineIndex++;
                        }
                        if (i < data.size() - 1) {
                            song.lines.add(parseLine(line.split("<"), nextLine, isLastLine));
                        } else {
                            song.lines.add(parseLine(line.split("<"), nextLine, isLastLine));
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

        return song;
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
            currentLine.syllables.add(syllable);
            endTime = startTime;
        }
        currentLine.to = endTime;
        return currentLine;
    }

    private static Song.Line parseLine(String[] line, String nextLine, boolean lastLine) {
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
                syllable.text = wordAndTime[1];
            }
            if (i < line.length - 1) {
                endTime = parseTimeStamp(line[i + 1].split(">")[0]);
            } else {
                if (lastLine) {
                    endTime = startTime + 100000;
                } else {
                    endTime = getLineTimeStamp(nextLine);
                }
            }
            syllable.from = startTime;
            syllable.to = endTime;
            currentLine.syllables.add(syllable);
            startTime = endTime;
        }
        currentLine.to = endTime;
        return currentLine;
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
        int pos = Integer.valueOf(split[1]);
        int len = Integer.valueOf(split[2]);
        s.tone = Integer.valueOf(split[3]);
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
