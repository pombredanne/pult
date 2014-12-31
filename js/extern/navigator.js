var Navigator;
/**
  @param {VibrationPattern} pattern
  @return {boolean}
  @see http://www.w3.org/TR/vibration/
*/
Navigator.prototype.vibrate = function(pattern) {};

hack from: http://stackoverflow.com/questions/15465263/google-closure-compiler-advanced-optimization-is-munging-navigator-battery-level
var navigator = {};
navigator.vibrate = function(pattern) {};
