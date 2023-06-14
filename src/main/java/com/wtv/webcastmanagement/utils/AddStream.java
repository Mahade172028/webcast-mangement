package com.wtv.webcastmanagement.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.wtv.webcastmanagement.dto.dto.KollectiveResponse;
import com.wtv.webcastmanagement.entity.WebcastPayload;
import com.wtv.webcastmanagement.entity.ecdn.Ecdn;
import com.wtv.webcastmanagement.entity.legacy.StreamInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AddStream {
    private final Kollective kollective;
    private final EnvironmentProperties envFiles;
    public StreamInfo audioPreviewStreams(boolean enableCdn, String language, String url) {
        String link = url != null ? convertAudioPreviewStreams(enableCdn, url) :
                ((enableCdn ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl()) + "/streamstudio/vod,"+StaticValues.audio_trailer_url);
        return getStreamAudio(language , link , "96");
    }

    public String convertAudioPreviewStreams(boolean enableChinaCdn, String url) {
        boolean find = url.contains(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaBackupUrlForPreview());
        if (find) {
            return url.replace(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaBackupUrlForPreview(),
                    enableChinaCdn ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl());
        } else {
            String splitUrl[] = url.split(",");
            String split1Part = splitUrl[1];
            try {
                DecodedJWT isKollective = JWT.decode(splitUrl[1]);
                if (isKollective != null) {
                    split1Part = StaticValues.audio_trailer_url;
                }
            } catch (Exception e) {

            }
            return (enableChinaCdn ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl()) + "/streamstudio/vod," + split1Part;
        }
    }

    public StreamInfo videoPreviewStreams(String bitRate, boolean enableCdn, String language, String url) {
        String bitRateSplit[] = bitRate.split(",");
        String link = url != null ? convertVideoPreviewStreams(enableCdn, url) :
                ((enableCdn ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl()) + "/trailer/vod," + StaticValues.video_trailer_url);
        Integer bitRateQuality = bitRateSplit.length > 0 ? Integer.valueOf(bitRateSplit[0]) : 1000;
        return getStream(language , link , String.valueOf(bitRateQuality));
    }

    public String convertVideoPreviewStreams(boolean enableChinaCdn, String url) {
        boolean find = url.contains(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaBackupUrlForPreview());
        if (find) {
            return url.replace(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaBackupUrlForPreview(),
                    enableChinaCdn ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl());
        } else {
            String splitUrl[] = url.split(",");
            String split1Part = splitUrl[1];
            try {
                DecodedJWT isKollective = JWT.decode(splitUrl[1]);
                if (isKollective != null) {
                    split1Part = StaticValues.video_trailer_url;
                }
            } catch (Exception e) {

            }
            return (enableChinaCdn ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl()) + "/trailer/vod," + split1Part;
        }
    }

    public StreamInfo audioOnDemandStreams(boolean enableCdn, String language) {
        String link = (enableCdn ? envFiles.chinaOnDemandUrl() : envFiles.wowzaOnDemandUrl()) + "/streamstudio/vod,"+StaticValues.audio_trailer_url;
        return getStreamAudio(language , link , "96");
    }

    public String convertAudioOnDemandStreams(boolean enableChinaCdn, String url) {
        boolean find = url.contains(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaOnDemandUrl());
        if (find) {
            return url.replace(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaOnDemandUrl(),
                    enableChinaCdn ? envFiles.chinaOnDemandUrl() : envFiles.wowzaOnDemandUrl());
        } else {
            String splitUrl[] = url.split(",");
            String split1Part = splitUrl[1];
            try {
                DecodedJWT isKollective = JWT.decode(splitUrl[1]);
                if (isKollective != null) {
                    split1Part = StaticValues.audio_trailer_url;
                }
            } catch (Exception e) {
            }
            return (enableChinaCdn ? envFiles.chinaOnDemandUrl() : envFiles.wowzaOnDemandUrl()) + "/streamstudio/vod," + split1Part;
        }
    }

    public StreamInfo videoOnDemandStreams(String bitRate, boolean enableCdn, String language) {
        String bitRateSplit[] = bitRate.split(",");
        String link = (enableCdn ? envFiles.chinaOnDemandUrl() : envFiles.wowzaOnDemandUrl()) + "/trailer/vod,"+StaticValues.video_trailer_url;
        Integer bitRateQuality = bitRateSplit.length > 0 ? Integer.valueOf(bitRateSplit[0]) : 1000;
        return getStream(language , link , String.valueOf(bitRateQuality));
    }

    public String convertVideoOnDemandStreams(boolean enableChinaCdn, String url) {
        boolean find = url.contains(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaOnDemandUrl());
        if (find) {
            return url.replace(enableChinaCdn ? envFiles.wowzaOnDemandUrl() : envFiles.chinaOnDemandUrl(),
                    enableChinaCdn ? envFiles.chinaOnDemandUrl() : envFiles.wowzaOnDemandUrl());
        } else {
            String splitUrl[] = url.split(",");
            String split1Part = splitUrl[1];
            try {
                DecodedJWT isKollective = JWT.decode(splitUrl[1]);
                if (isKollective != null) {
                    split1Part = StaticValues.video_trailer_url;
                }
            } catch (Exception e) {

            }
            return (enableChinaCdn ? envFiles.chinaOnDemandUrl() : envFiles.wowzaOnDemandUrl()) + "/trailer/vod," + split1Part;
        }
    }


    public List<StreamInfo> audioLiveStreams(List<String> langs, String webcastId, boolean enableChinaCDN, List<StreamInfo> liveStreamList) {

        String hostUrl = enableChinaCDN ? envFiles.chinaMainUrl() : envFiles.mainUrl();
        String hostBackupUrl = enableChinaCDN ? envFiles.chinaBackupUrl() : envFiles.backupUrl();
        List<String> bitRateSplit = List.of(("96,").split(","));

        Function<String, String> hlsMainString = (l) -> {
            return bitRateSplit.stream().map(g -> webcastId + "_" + l + "_p_" + g).collect(Collectors.joining(","));
        };
        Function<String, String> hlsBackupString = (l) -> {
            return bitRateSplit.stream().map(g -> webcastId + "_" + l + "_b_" + g).collect(Collectors.joining(","));
        };
        Function<List<String>, String> modifyUrl = (values) -> {
            String url = values.get(0);
            String lang = values.get(1);
            String hostType = values.get(2);
            String splitStreamUrl[] = url.split(",");
            if (splitStreamUrl.length == 2) {
                if (hostType.equals("main")) {
                    return hostUrl +","+ splitStreamUrl[1];
                } else {
                    return hostBackupUrl +","+ splitStreamUrl[1];
                }
            } else {
                if (hostType.equals("main")) {
                    return hostUrl +","+ hlsMainString.apply(lang);
                } else {
                    return hostBackupUrl +","+ hlsBackupString.apply(lang);
                }
            }
        };

        List<StreamInfo> items = new ArrayList<>();

        for (String lang : langs) {
            String finalStreamUrl = null, finalStreamBackupUrl = null;
            if (liveStreamList.stream().filter(p -> p.getUrl().equals(lang)).collect(Collectors.toList()).size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getLanguage().equals(lang) && ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
                Optional<StreamInfo> backup = liveStreamList.stream().filter(ps -> ps.getLanguage().equals(lang) && ps.getUrl().contains("_" + lang + "_b_")).findFirst();
                if (backup.isPresent()) {
                    finalStreamBackupUrl = modifyUrl.apply(List.of(backup.get().getUrl(), backup.get().language, "backup"));
                }
            } else if (liveStreamList.size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
                Optional<StreamInfo> backup = liveStreamList.stream().filter(ps -> ps.getUrl().contains("_" + lang + "_b_")).findFirst();
                if (backup.isPresent()) {
                    finalStreamBackupUrl = modifyUrl.apply(List.of(backup.get().getUrl(), backup.get().language, "backup"));
                }
            }

            if (finalStreamUrl == null) {
                finalStreamUrl = hostUrl +","+ hlsMainString.apply(lang);
            }
            if (finalStreamBackupUrl == null) {
                finalStreamBackupUrl = hostBackupUrl +","+ hlsBackupString.apply(lang);
            }
            String finalStreamUrl1 = finalStreamUrl;
            String finalStreamBackupUrl1 = finalStreamBackupUrl;
            bitRateSplit.forEach(e -> {
                items.add(getStreamAudio(lang, finalStreamUrl1, e));
                items.add(getStreamAudio(lang, finalStreamBackupUrl1, e));
            });
        }
        return items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());

    }

    public String convertLiveStreams(boolean enableChinaCDN, String lang, String url, String webcastType) {
        if (webcastType.equals("zoom")) {
            boolean find = url.contains(enableChinaCDN ? envFiles.mainZoomUrl() : envFiles.chinaMainZoomUrl());
            if (find) {
                return url.replace(enableChinaCDN ? envFiles.mainZoomUrl() : envFiles.chinaMainZoomUrl(),
                        enableChinaCDN ? envFiles.chinaMainZoomUrl() : envFiles.mainZoomUrl());
            } else {
                String split[] = url.split(",");
                return (enableChinaCDN ? envFiles.chinaMainZoomUrl() : envFiles.mainZoomUrl()) +","+ split[1];
            }
        } else {
            boolean main = url.contains("_" + lang + "_p_");
            if (main) {
                boolean find = url.contains(enableChinaCDN ? envFiles.mainUrl() : envFiles.chinaMainUrl());
                if (find) {
                    return url.replace(enableChinaCDN ? envFiles.mainUrl() : envFiles.chinaMainUrl(),
                            enableChinaCDN ? envFiles.chinaMainUrl() : envFiles.mainUrl());
                } else {
                    String split[] = url.split(",");
                    return (enableChinaCDN ? envFiles.chinaMainUrl() : envFiles.mainUrl()) +","+ split[1];
                }
            } else {
                boolean find = url.contains(enableChinaCDN ? envFiles.backupUrl() : envFiles.chinaBackupUrl());
                if (find) {
                    return url.replace(enableChinaCDN ? envFiles.backupUrl() : envFiles.chinaBackupUrl(),
                            enableChinaCDN ? envFiles.chinaBackupUrl() : envFiles.backupUrl());
                } else {
                    String split[] = url.split(",");
                    return (enableChinaCDN ? envFiles.chinaBackupUrl() : envFiles.backupUrl()) +","+ split[1];
                }
            }
        }

    }


    public List<StreamInfo> generateStream(String bitRate, List<String> langs, String webcastId, boolean enableChinaCDN, List<StreamInfo> liveStreamList) {

        String hostUrl = enableChinaCDN ? envFiles.chinaMainUrl() : envFiles.mainUrl();
        String hostBackupUrl = enableChinaCDN ? envFiles.chinaBackupUrl() : envFiles.backupUrl();
        List<String> bitRateSplit = List.of(bitRate.split(","));

        Function<String, String> hlsMainString = (l) -> {
            return bitRateSplit.stream().map(g -> webcastId + "_" + l + "_p_" + g).collect(Collectors.joining(","));
        };
        Function<String, String> hlsBackupString = (l) -> {
            return bitRateSplit.stream().map(g -> webcastId + "_" + l + "_b_" + g).collect(Collectors.joining(","));
        };
        Function<List<String>, String> modifyUrl = (values) -> {
            String url = values.get(0);
            String lang = values.get(1);
            String hostType = values.get(2);
            String splitStreamUrl[] = url.split(",");
            if (splitStreamUrl.length == 2) {
                if (hostType.equals("main")) {
                    return hostUrl +","+ splitStreamUrl[1];
                } else {
                    return hostBackupUrl +","+ splitStreamUrl[1];
                }
            } else {
                if (hostType.equals("main")) {
                    return hostUrl +","+ hlsMainString.apply(lang);
                } else {
                    return hostBackupUrl +","+ hlsBackupString.apply(lang);
                }
            }
        };

        List<StreamInfo> items = new ArrayList<>();

        for (String lang : langs) {
            String finalStreamUrl = null, finalStreamBackupUrl = null;
            if (liveStreamList.stream().filter(p -> p.getUrl().equals(lang)).collect(Collectors.toList()).size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getLanguage().equals(lang) && ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
                Optional<StreamInfo> backup = liveStreamList.stream().filter(ps -> ps.getLanguage().equals(lang) && ps.getUrl().contains("_" + lang + "_b_")).findFirst();
                if (backup.isPresent()) {
                    finalStreamBackupUrl = modifyUrl.apply(List.of(backup.get().getUrl(), backup.get().language, "backup"));
                }
            } else if (liveStreamList.size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
                Optional<StreamInfo> backup = liveStreamList.stream().filter(ps -> ps.getUrl().contains("_" + lang + "_b_")).findFirst();
                if (backup.isPresent()) {
                    finalStreamBackupUrl = modifyUrl.apply(List.of(backup.get().getUrl(), backup.get().language, "backup"));
                }
            }

            if (finalStreamUrl == null) {
                finalStreamUrl = hostUrl +","+ hlsMainString.apply(lang);
            }
            if (finalStreamBackupUrl == null) {
                finalStreamBackupUrl = hostBackupUrl +","+ hlsBackupString.apply(lang);
            }
            String finalStreamUrl1 = finalStreamUrl;
            String finalStreamBackupUrl1 = finalStreamBackupUrl;
            bitRateSplit.forEach(e -> {
                items.add(getStream(lang, finalStreamUrl1, e));
                items.add(getStream(lang, finalStreamBackupUrl1, e));
            });
        }
        return items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());

    }


    public List<StreamInfo> generateZoomStream(String bitRate, List<String> langs, String webcastId, boolean enableChinaCDN, List<StreamInfo> liveStreamList) {

        List<String> bitRateSplit = List.of(bitRate.split(","));
        String hostUrl = (enableChinaCDN ? envFiles.chinaMainZoomUrl() : envFiles.mainZoomUrl()) + (bitRateSplit.size() > 0 ? bitRateSplit.get(0) : 1000);

        Function<String, String> hlsMainString = (l) -> {
            return bitRateSplit.stream().map(g -> webcastId + "_" + l + "_p_" + g).collect(Collectors.joining(","));
        };
        Function<List<String>, String> modifyUrl = (values) -> {
            String url = values.get(0);
            String lang = values.get(1);
            String splitStreamUrl[] = url.split(",");
            if (splitStreamUrl.length == 2) {
                return hostUrl +","+ splitStreamUrl[1];
            } else {
                return hostUrl +","+ hlsMainString.apply(lang);
            }
        };
        List<StreamInfo> items = new ArrayList<>();

        for (String lang : langs) {
            String finalStreamUrl = null;
            if (liveStreamList.stream().filter(p -> p.getUrl().equals(lang)).collect(Collectors.toList()).size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getLanguage().equals(lang) && ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
            } else if (liveStreamList.size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
            }
            if (finalStreamUrl == null) {
                finalStreamUrl = hostUrl +","+ hlsMainString.apply(lang);
            }
            String finalStreamUrl1 = finalStreamUrl.replace("_" + lang + "_p_", "_en_p_");
            bitRateSplit.forEach(e -> {
                items.add(getStream(lang, finalStreamUrl1, e));
            });
        }
        //items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());
        return items;
    }


    public List<StreamInfo> generateAudioZoomStream(String bitRate, List<String> langs, String webcastId, boolean enableChinaCDN, List<StreamInfo> liveStreamList) {

        List<String> bitRateSplit = List.of(bitRate.split(","));
        String hostUrl = (enableChinaCDN ? envFiles.chinaMainZoomUrl() : envFiles.mainZoomUrl()) + (bitRateSplit.size() > 0 ? bitRateSplit.get(0) : 96);

        Function<String, String> hlsMainString = (l) -> {
            return bitRateSplit.stream().map(g -> webcastId + "_" + l + "_p_" + g).collect(Collectors.joining(","));
        };
        Function<List<String>, String> modifyUrl = (values) -> {
            String url = values.get(0);
            String lang = values.get(1);
            String splitStreamUrl[] = url.split(",");
            if (splitStreamUrl.length == 2) {
                return hostUrl +","+ splitStreamUrl[1];
            } else {
                return hostUrl +","+ hlsMainString.apply(lang);
            }
        };
        List<StreamInfo> items = new ArrayList<>();

        for (String lang : langs) {
            String finalStreamUrl = null;
            if (liveStreamList.stream().filter(p -> p.getLanguage().equals(lang)).collect(Collectors.toList()).size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getLanguage().equals(lang) && ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
            } else if (liveStreamList.size() > 1) {
                Optional<StreamInfo> main = liveStreamList.stream().filter(ps -> ps.getUrl().contains("_" + lang + "_p_")).findFirst();
                if (main.isPresent()) {
                    finalStreamUrl = modifyUrl.apply(List.of(main.get().getUrl(), main.get().language, "main"));
                }
            }
            if (finalStreamUrl == null) {
                finalStreamUrl = hostUrl +","+ hlsMainString.apply(lang);
            }
            String finalStreamUrl1 = finalStreamUrl.replace("_" + lang + "_p_", "_en_p_");
            bitRateSplit.forEach(e -> {
                items.add(getStream(lang, finalStreamUrl1, e));
            });
        }
        //items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());
        return items;
    }


    public List<StreamInfo> generateAudioZoomStreamWithKollectiveECDN(
            List<String> langs, String type, String bitRate, String id, String title, Ecdn ecdnEntry, boolean enableChinaCDN) {
        List<StreamInfo> items = new ArrayList<>();
        String bitRateSplit[] = bitRate.split(",");
        for (String lang : langs) {
            KollectiveResponse contentToken = kollective.generateKollectiveTokenforZoom("en", bitRate, id,
                    title, ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                    ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(), enableChinaCDN);

            if (contentToken == null) return null;
            String link = "_kollective_," + contentToken.getContentToken();
            Integer bitRateQuality = bitRateSplit.length > 0 ? Integer.valueOf(bitRateSplit[0]) : 96;
            items.add(getStreamAudio(lang , link , String.valueOf(bitRateQuality)));
        }
        items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());
        return items;
    }

    public List<StreamInfo> generateZoomStreamWithKollectiveECDN(
            List<String> langs, String type, String bitRate, String id, String title, Ecdn ecdnEntry, boolean enableChinaCDN) {
        List<StreamInfo> items = new ArrayList<>();
        String bitRateSplit[] = bitRate.split(",");
        for (String lang : langs) {
            KollectiveResponse contentToken = kollective.generateKollectiveTokenforZoom("en", bitRate, id,
                    title, ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                    ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(), enableChinaCDN);

            if (contentToken == null) return null;
            String link = "_kollective_," + contentToken.getContentToken();
            Integer bitRateQuality = bitRateSplit.length > 0 ? Integer.valueOf(bitRateSplit[0]) : 1000;
            items.add(getStream(lang , link , String.valueOf(bitRateQuality)));
        }
        items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());
        return items;
    }


    public List<StreamInfo> generateStreamWithKollectiveECDN(
            List<String> langs, String type, String bitRate, String id, String title, Ecdn ecdnEntry, boolean enableChinaCDN) {
        List<StreamInfo> items = new ArrayList<>();
        String bitRateSplit[] = bitRate.split(",");
        for (String lang : langs) {
            KollectiveResponse contentToken = kollective.generateKollectiveTokenforSelf(lang, bitRate, id,
                    title, ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                    ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(), enableChinaCDN);

            if (contentToken == null) return null;
            String link = "_kollective_," + contentToken.getContentToken();
            Integer bitRateQuality = bitRateSplit.length > 0 ? Integer.valueOf(bitRateSplit[0]) : 1000;
            items.add(getStream(lang , link , String.valueOf(bitRateQuality)));
        }
        items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());
        return items;
    }

    public List<StreamInfo> generateAudioStreamWithKollectiveECDN(
            List<String> langs, String type, String bitRate, String id, String title, Ecdn ecdnEntry, boolean enableChinaCDN) {
        List<StreamInfo> items = new ArrayList<>();
        String bitRateSplit[] = bitRate.split(",");
        for (String lang : langs) {
            KollectiveResponse contentToken = kollective.generateKollectiveTokenforSelfAudioOnly(lang, bitRate, id,
                    title, ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                    ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(), enableChinaCDN);

            if (contentToken == null) return null;
            String link = "_kollective_," + contentToken.getContentToken();
            Integer bitRateQuality = bitRateSplit.length > 0 ? Integer.valueOf(bitRateSplit[0]) : 96;
            items.add(getStreamAudio(lang , link , String.valueOf(bitRateQuality)));
        }
        items.stream().filter(distinctByKey(StreamInfo::getUrl)).collect(Collectors.toList());
        return items;
    }


    public List<StreamInfo> generatePreviewStreamsUpsert(WebcastPayload dataWebcast, WebcastPayload currentDataWebcast) {
        try {
            String webcastType = dataWebcast.getWebcastType();
            boolean isECDNActive = dataWebcast.isEnableECDNActive();
            List<StreamInfo> previewStreamsList = dataWebcast.getPreviewStreamList();
            boolean enableChinaCDN = dataWebcast.isEnableChinaCDN();
            String bitRate = dataWebcast.getBitRate();
            String defaultLanguage = dataWebcast.getDefaultLanguage();
            List<String> additionalLanguages = dataWebcast.getAdditionalLanguage();
            boolean changeWebcastType = dataWebcast.getChangeWebcastType();
            boolean isAudioWebcast = List.of("audioSlide", "audio").contains(webcastType);
            boolean isCurrentAudioWebcast = List.of("audioSlide", "audio").contains(currentDataWebcast.getWebcastType());
            List<StreamInfo> previousStreams = previewStreamsList;

            String hostUrl = enableChinaCDN ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl();
            String audioHostPart = hostUrl + "/streamstudio/vod";
            String audioTrackPart = StaticValues.audio_trailer_url;
            String audioUrl = audioHostPart + "," + audioTrackPart;
            String videoHostPart = hostUrl + "/trailer/vod";
            String videoTrackPart = StaticValues.video_trailer_url;
            String videoUrl = videoHostPart + "," + videoTrackPart;
            Function<String, String> modifyUrl = (url) -> {
                String splitStreamUrl[] = url.split(",");
                if (splitStreamUrl.length == 2) {
                    String splitHostStreamUrl = isAudioWebcast ? audioHostPart : videoHostPart;
                    if (splitStreamUrl[0].contains("od.world-television.com")) {
                        splitHostStreamUrl = splitStreamUrl[0].replace("od.world-television.com", hostUrl);
                    }
                    return splitHostStreamUrl + "," + splitStreamUrl[1];
                } else {
                    return isAudioWebcast ? audioUrl : videoUrl;
                }
            };

            HashSet<String> allLanguage = new HashSet<>(additionalLanguages);
            allLanguage.add(defaultLanguage);

            return allLanguage.stream().map(language -> {
                String finalStreamUrl = null;
                if (webcastType != null || changeWebcastType || isAudioWebcast != isCurrentAudioWebcast || !isECDNActive || previousStreams.isEmpty()) {
                    finalStreamUrl = isAudioWebcast ? audioUrl : videoUrl;
                } else if (previousStreams.stream().filter(p -> p.getLanguage().equals(language)).collect(Collectors.toList()).size() > 1) {
                    Optional<StreamInfo> itemUrl = previousStreams.stream().filter(ps -> ps.getLanguage().equals(language)).findFirst();
                    finalStreamUrl = modifyUrl.apply(itemUrl.get().getUrl());
                } else if (!previousStreams.isEmpty()) {
                    finalStreamUrl = modifyUrl.apply(previousStreams.get(0).getUrl());
                } else {
                    finalStreamUrl = isAudioWebcast ? audioUrl : videoUrl;
                }
                String bitRateSplit[] = bitRate.split(",");
                if (isAudioWebcast) {
                    return getStreamAudio(language, finalStreamUrl, bitRateSplit.length > 0 ? bitRateSplit[0] : "96");
                } else {
                    return getStream(language, finalStreamUrl, bitRateSplit.length > 0 ? bitRateSplit[0] : "1000");
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    public List<StreamInfo> generateOndemandStreamsUpsert(WebcastPayload dataWebcast, WebcastPayload currentDataWebcast) {
        try {
            String webcastType = dataWebcast.getWebcastType();
            boolean isECDNActive = dataWebcast.isEnableECDNActive();
            List<StreamInfo> previewStreamsList = dataWebcast.getOndemandStreamsList();
            boolean enableChinaCDN = dataWebcast.isEnableChinaCDN();
            String bitRate = dataWebcast.getBitRate();
            String defaultLanguage = dataWebcast.getDefaultLanguage();
            List<String> additionalLanguages = dataWebcast.getAdditionalLanguage();
            boolean changeWebcastType = dataWebcast.getChangeWebcastType();
            boolean isAudioWebcast = List.of("audioSlide", "audio").contains(webcastType);
            boolean isCurrentAudioWebcast = List.of("audioSlide", "audio").contains(currentDataWebcast.getWebcastType());
            List<StreamInfo> previousStreams = previewStreamsList;

            String hostUrl = enableChinaCDN ? envFiles.chinaBackupUrl() : envFiles.wowzaOnDemandUrl();
            String audioHostPart = hostUrl + "/streamstudio/vod";
            String audioTrackPart = StaticValues.audio_trailer_url;
            String audioUrl = audioHostPart + "," + audioTrackPart;
            String videoHostPart = hostUrl + "/trailer/vod";
            String videoTrackPart = StaticValues.video_trailer_url;
            String videoUrl = videoHostPart + "," + videoTrackPart;
            Function<String, String> modifyUrl = (url) -> {
                String splitStreamUrl[] = url.split(",");
                if (splitStreamUrl.length == 2) {
                    String splitHostStreamUrl = isAudioWebcast ? audioHostPart : videoHostPart;
                    if (splitStreamUrl[0].contains("od.world-television.com")) {
                        splitHostStreamUrl = splitStreamUrl[0].replace("od.world-television.com", hostUrl);
                    }
                    return splitHostStreamUrl + "," + splitStreamUrl[1];
                } else {
                    return isAudioWebcast ? audioUrl : videoUrl;
                }
            };

            HashSet<String> allLanguage = new HashSet<>(additionalLanguages);
            allLanguage.add(defaultLanguage);

            return allLanguage.stream().map(language -> {
                String finalStreamUrl = null;
                if (webcastType != null || changeWebcastType || isAudioWebcast != isCurrentAudioWebcast || !isECDNActive || previousStreams.isEmpty()) {
                    finalStreamUrl = isAudioWebcast ? audioUrl : videoUrl;
                } else if (previousStreams.stream().filter(p -> p.getLanguage().equals(language)).collect(Collectors.toList()).size() > 1) {
                    Optional<StreamInfo> itemUrl = previousStreams.stream().filter(ps -> ps.getLanguage().equals(language)).findFirst();
                    finalStreamUrl = modifyUrl.apply(itemUrl.get().getUrl());
                } else if (!previousStreams.isEmpty()) {
                    finalStreamUrl = modifyUrl.apply(previousStreams.get(0).getUrl());
                } else {
                    finalStreamUrl = isAudioWebcast ? audioUrl : videoUrl;
                }
                String bitRateSplit[] = bitRate.split(",");
                if (isAudioWebcast) {
                    return getStreamAudio(language, finalStreamUrl, bitRateSplit.length > 0 ? bitRateSplit[0] : "96");
                } else {
                    return getStream(language, finalStreamUrl, bitRateSplit.length > 0 ? bitRateSplit[0] : "1000");
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    public List<StreamInfo> previewStreamsMultipleLanguages(List<String> languages, String bitRate, boolean enableChinaCDN,
                                                            String webcastType, List<StreamInfo> previewStreamList, boolean changeWebcastType) {
        try {
            String finalStreamUrl = "";
            if (changeWebcastType || previewStreamList.isEmpty()) {
                finalStreamUrl = webcastType.equals("audioSlide") || webcastType.equals("audio") ?
                        envFiles.standardPreviewAudioUrl() :
                        envFiles.standardPreviewVideoUrl();
            } else {
                String splitStreamUrl[] = previewStreamList.get(0).getUrl().split(",");
                String splitHostStreamUrl = "";
                if (splitStreamUrl[0].contains("od.world-television.com")) {
                    splitHostStreamUrl = splitStreamUrl[0].replace(
                            "od.world-television.com",
                            enableChinaCDN ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl());
                } else {
                    splitHostStreamUrl = splitStreamUrl[0].split("/")[0].replace(
                            splitStreamUrl[0].split("/")[0],
                            enableChinaCDN ? envFiles.chinaBackupUrlForPreview() : envFiles.wowzaOnDemandUrl());
                }
                finalStreamUrl = splitHostStreamUrl + "/" +
                        splitStreamUrl[0].split("/")[1] + "/" +
                        splitStreamUrl[0].split("/")[2] + ',' + splitStreamUrl[1];
            }
            String finalStreamUrl1 = finalStreamUrl;
            List<StreamInfo> streamInfoList = languages.stream().map(lang -> {
                String bitRateSplit[] = bitRate.split(",");
                if (webcastType.equals("audioSlide") || webcastType.equals("audio")) {
                    return getStreamAudio(lang, finalStreamUrl1, bitRateSplit.length > 0 ? bitRateSplit[0] : "96");
                } else {
                    return getStream(lang, finalStreamUrl1, bitRateSplit.length > 0 ? bitRateSplit[0] : "1000");
                }
            }).collect(Collectors.toList());
            return streamInfoList;
        } catch (Exception e) {
            return null;
        }
    }


    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public StreamInfo getStreamAudio(String lang, String finalStreamUrl, String quality) {
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setFormat("mobile");
        streamInfo.setMedia("audio");
        streamInfo.setLocale("default");
        streamInfo.setLanguage(lang);
        streamInfo.setEncoder("");
        streamInfo.setQuality(Integer.valueOf(quality));
        streamInfo.setProtocol("hls");
        streamInfo.setUrl(finalStreamUrl);
        streamInfo.setStartTime("0");
        streamInfo.setEndTime("0");
        streamInfo.setFramesize("");
        return streamInfo;
    }

    public StreamInfo getStream(String lang, String finalStreamUrl, String e) {
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setFormat("mobile");
        streamInfo.setMedia("video");
        streamInfo.setLocale("default");
        streamInfo.setLanguage(lang);
        streamInfo.setEncoder("");
        streamInfo.setQuality(Integer.valueOf(e));
        streamInfo.setProtocol("hls");
        streamInfo.setUrl(finalStreamUrl);
        streamInfo.setStartTime("0");
        streamInfo.setEndTime("0");
        streamInfo.setFramesize("768x432'");
        return streamInfo;
    }


}
