import urllib.request, json
# Test backend
try:
    r = urllib.request.urlopen("http://localhost:8080/api/v1/search?q=test")
    print("Backend OK:", r.read()[:200])
except Exception as e:
    print("Backend ERROR:", type(e).__name__, str(e)[:200])

# Test YouTube directly
try:
    req = urllib.request.Request("https://music.youtube.com/youtubei/v1/search?key=AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30&prettyPrint=false", data=json.dumps({"context":{"client":{"clientName":"WEB_REMIX","clientVersion":"1.20260707.12.00","hl":"en","gl":"US"}},"query":"test"}).encode(), headers={"Content-Type":"application/json"})
    r = urllib.request.urlopen(req)
    print("YouTube OK:", r.status, r.read()[:200])
except Exception as e:
    print("YouTube ERROR:", type(e).__name__, str(e)[:200])

# Test YouTube with ANDROID_MUSIC key
try:
    req = urllib.request.Request("https://music.youtube.com/youtubei/v1/search?key=AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI&prettyPrint=false", data=json.dumps({"context":{"client":{"clientName":"ANDROID_MUSIC","clientVersion":"7.27.52","androidSdkVersion":30,"osName":"Android","osVersion":"11","userAgent":"com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 11) gzip"}},"query":"test"}).encode(), headers={"Content-Type":"application/json"})
    r = urllib.request.urlopen(req)
    print("YouTube ANDROID OK:", r.status, r.read()[:200])
except Exception as e:
    print("YouTube ANDROID ERROR:", type(e).__name__, str(e)[:200])
