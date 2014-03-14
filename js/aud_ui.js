
var stress = 0.5;
var energy = 0.5;

function drawAUDpattern()
{
	var canvas = document.getElementById("canvas");
	var ctx=canvas.getContext('2d');

	ctx.fillStyle='#DDF';
	ctx.fillRect(0,0,1200,400);
	
    ctx.font = '6pt Courier';
    ctx.lineWidth = 0.5;
    ctx.strokeStyle = '#3366FF';
	
	ctx.lineWidth = 1;
	ctx.beginPath();
	for (var i = 0; i < aud.pattern.length; i++)
	{
		var type = i;
		switch (type) 
		{
			case 0: case 1: type = "Percussion"; break;
			case 2: case 3: case 4: case 5: type = "Melody 1"; break;
			case 6: case 7: case 8: case 9: type = "Melody 2"; break;
			case 10: case 11: case 12: case 13: type = "Melody 3"; break;
			case 14: case 15: case 16: case 17: type = "Melody 4"; break;
			default: type = "?";
		}
		ctx.strokeStyle = '#333366';
		ctx.strokeText("Instr. " + (i + 1) + " (" + type + ")", 5, 55 + i * 20);
		ctx.moveTo(0, 40 + i * 20);
		ctx.strokeStyle = '#3366FF';
		ctx.lineTo(1200, 40 + i * 20);		
	}
	ctx.stroke();
	
	ctx.beginPath();
	for (var j = aud.pattern_position; j < aud.pattern_position + 50 && j <= aud.pattern[0].pattern.length; j++)
	{
		if (j == aud.pattern[0].pattern.length)
		{
			ctx.fillStyle='rgba(200, 0, 0, 0.5)';
			ctx.fillRect(200 + (j - aud.pattern_position) * 25,40,1200,500);
		}
		else
		{
			ctx.strokeStyle = '#333366';
			if ((j % 32) == 0)
				ctx.strokeText("M" + Math.floor(j / 32), 200 + (j - aud.pattern_position) * 25, 20);
			ctx.strokeText("T" + (j % 32), 200 + (j - aud.pattern_position) * 25, 35);
			ctx.strokeStyle = '#3366FF';
			ctx.moveTo(200 + (j - aud.pattern_position)  * 25, 40);
			ctx.lineTo(200 + (j - aud.pattern_position) * 25, 500);
			
			for (var t = 0; t < aud.pattern.length; t++)
			{
				var temp = aud.pattern[t].pattern[j];
				if (temp != -1)
				{
					var note;
					var bar = Math.floor(temp / 12);
					switch (temp % 12)
					{
						case 0: note = 'C'; break;   case 4: note = 'E'; break;  case 8: note = 'G#'; break;
						case 1: note = 'C#'; break;  case 5: note = 'F'; break;  case 9: note = 'A'; break;
						case 2: note = 'D'; break;   case 6: note = 'F#'; break; case 10: note = 'A#'; break;
						case 3: note = 'D#'; break;  case 7: note = 'G'; break;  case 11: note = 'B'; break;
					}
					
					ctx.strokeStyle = '#3366FF';
					ctx.strokeText(note + "" + bar, 205 + (j - aud.pattern_position) * 25, 55 + t * 20);
				}
			}
		}
	}
	ctx.stroke();
}

function resumepause()
{
	aud.togglePause();
}
function playstop()
{
	aud.togglePlay();
}

var seed = 0;
var pat = 1;
var patlen = 1;

function generate()
{
	aud.generatePattern(stress, energy, pat, patlen, seed);
	
	drawAUDpattern();
}	
function adapt()
{
	aud.adaptPattern(stress, energy);
	
	drawAUDpattern();
}	

function updateSeed()
{
	var s = document.getElementById("seed");
	var sd = parseInt(s.value);
	if (isNaN(sd))
	{
		seed = 0;
		log.innerHTML = "Seed - incorrect format";
	}
	else
	{
		seed = sd;
		log.innerHTML = "Seed - set to " + sd;
	}
}

function updatePat()
{
	var p = document.getElementById("pat");
	var pd = parseInt(p.value);
	if (isNaN(pd) || pd < 1 || pd > 100)
	{
		pat = 1;
		log.innerHTML = "# of patterns - incorrect format";
	}
	else
	{
		pat = Math.floor(pd);
		log.innerHTML = "# of patterns - set to " + pat;
	}
}

function updatePatlen()
{
	var p = document.getElementById("patlen");
	var pd = parseInt(p.value);
	if (isNaN(pd) || pd < 1 || pd > 8)
	{
		patlen = 4;
		log.innerHTML = "Pattern repeats - incorrect format";
	}
	else
	{
		patlen = Math.floor(pd);
		log.innerHTML = "Pattern Repeats - set to " + patlen;
	}
}

function updateStress()
{
	var log = document.getElementById("log");

	var s = document.getElementById("stress");
	var str = parseFloat(s.value);
	if (str > 1 || str < 0 || isNaN(str))
	{
		stress = 0.5;
		log.innerHTML = "Stress - incorrect format";
	}
	else 
	{
		stress = str;
		log.innerHTML = "Stress - set to " + str;
	}
	drawCanvasBasic_emote();
}

function updateEnergy()
{
	var log = document.getElementById("log");
	
	var e = document.getElementById("energy");
	var en = parseFloat(e.value);
	if (en > 1 || en < 0 || isNaN(en))
	{
		energy = 0.5;
		log.innerHTML = "Energy - incorrect format";
	}
	else
	{
		energy = en;
		log.innerHTML = "Energy - set to " + en;
	}
	drawCanvasBasic_emote();
}

function updateStrEng(event)
{
	var canvas = document.getElementById("emote_canvas");
	var rect = canvas.getBoundingClientRect();

	stress = ((event.pageX - rect.left) / 200).toFixed(3);
	energy = ((200 - event.pageY + rect.top) / 200).toFixed(3);
	
	var e = document.getElementById("energy");
	e.value = energy + "";
	
	var s = document.getElementById("stress");
	s.value = stress + "";
	
	drawCanvasBasic_emote();
	
	var log = document.getElementById("log");
	
	log.innerHTML = "Stress - set to " + stress + ", Energy - set to " + energy;
	
	var en = parseFloat(e.value);
	energy = en;
	var str = parseFloat(s.value);
	stress = str;
}

function drawCanvasBasic_emote()
{
	var canvas = document.getElementById("emote_canvas");
	
	var ctx=canvas.getContext('2d');
	ctx.fillStyle='#DDF';
	ctx.fillRect(0,0,200,200);
	
    ctx.strokeStyle = '#3366FF';
	ctx.lineWidth = 0.5;
	
	ctx.beginPath();
	ctx.moveTo(5, 100);
	ctx.lineTo(195, 100);
	ctx.lineTo(190, 95);
	ctx.moveTo(195, 100);
	ctx.lineTo(190, 105);
    ctx.stroke();
	
	ctx.beginPath();
	ctx.moveTo(100, 195);
	ctx.lineTo(100, 5);
	ctx.lineTo(95, 10);
	ctx.moveTo(100, 5);
	ctx.lineTo(105, 10);
    ctx.stroke();
	
	ctx.font = '6pt Courier';
    ctx.lineWidth = 0.5;
    ctx.strokeStyle = '#0033CC';
    ctx.strokeText('stress', 10, 115);
    ctx.strokeText('energy', 105, 15);
	
    ctx.lineWidth = 1.5;
    ctx.strokeStyle = '#AA3333';
	ctx.beginPath();
	ctx.moveTo((stress) * 200 + 5, (1 - energy) * 200);
	ctx.lineTo((stress) * 200 - 5, (1 - energy) * 200);
	ctx.moveTo((stress) * 200, (1 - energy) * 200 + 5);
	ctx.lineTo((stress) * 200, (1 - energy) * 200 - 5);
    ctx.stroke();
}


aud.ontick = drawAUDpattern;

window.onload = function()
{
	drawCanvasBasic_emote();
	drawAUDpattern();
	
	document.getElementById('emote_canvas').addEventListener('click', function(event) {	updateStrEng(event); });
}

var savePiece = function()
{
	var date = new Date();
	var name = (date.getDate()) + "-" + (date.getMonth() + 1) + "-" + (date.getFullYear()) + "--" + 
		(date.getHours()) + "-" + (date.getMinutes()) + "-" + (date.getSeconds()) + ".wav";
	
	var data = encodePiece();
	
	saveAs(new Blob([data], {type:'audio/wav'}), name);
}

var encodePiece = function()
{
	var num = aud.getNumSamples();
	var samples = new Array();
	
	var ndx = 0;
	for (var i = 0; i < num; i += aud.interf.speed)
	{
		samples[ndx++] = aud.interf.soundAt(i);
	}

	var wav = encodeWAV(samples);
	return wav;
}

function floatTo16BitPCM(output, offset, input){
  for (var i = 0; i < input.length; i++, offset+=2){
    var s = Math.max(-1, Math.min(1, input[i]));
    output.setInt16(offset, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
  }
}
function floatTo8BitPCM(output, offset, input){
  for (var i = 0; i < input.length; i++, offset+=1){
    var s = Math.max(-1, Math.min(1, input[i]));
    output.setInt8(offset, s < 0 ? s * 0x80 : s * 0x7F, true);
  }
}

function writeString(view, offset, string){
  for (var i = 0; i < string.length; i++){
    view.setUint8(offset + i, string.charCodeAt(i));
  }
}

function encodeWAV(samples){
  var buffer = new ArrayBuffer(44 + samples.length * 2);
  var view = new DataView(buffer);

  /* RIFF identifier */
  writeString(view, 0, 'RIFF');
  /* file length */
  view.setUint32(4, 32 + samples.length * 2, true);
  /* RIFF type */
  writeString(view, 8, 'WAVE');
  /* format chunk identifier */
  writeString(view, 12, 'fmt ');
  /* format chunk length */
  view.setUint32(16, 16, true);
  /* sample format (raw) */
  view.setUint16(20, 1, true);
  /* channel count */
  view.setUint16(22, 1, true);
  /* sample rate */
  view.setUint32(24, sampleRate, true);
  /* byte rate (sample rate * block align) */
  view.setUint32(28, sampleRate * 2, true);
  /* block align (channel count * bytes per sample) */
  view.setUint16(32, 2, true);
  /* bits per sample */
  view.setUint16(34, 16, true);
  /* data chunk identifier */
  writeString(view, 36, 'data');
  /* data chunk length */
  view.setUint32(40, samples.length * 2, true);

  floatTo16BitPCM(view, 44, samples);

  return view;
}
