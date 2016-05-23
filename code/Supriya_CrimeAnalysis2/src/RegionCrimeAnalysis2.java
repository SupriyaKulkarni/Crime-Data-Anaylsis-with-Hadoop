import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class RegionCrimeAnalysis2 {
	public static class CrimeAnalysisDescriptionOne extends
	Mapper<LongWritable, Text, Text, IntWritable> {
public void map(LongWritable key, Text line, Context Mapcontext)
		throws IOException, InterruptedException {
	
	String inputLine= String.valueOf(line).trim(); //Converting the input line to string 
	
	if(!(inputLine.contains("Easting") && inputLine.contains("Northing") && inputLine.contains("Crime ID") && inputLine.contains("Reported By") && inputLine.contains("Month") )){
		String[] DataInLine = inputLine.split(",");
		String Ctype="";
		String Easting="";
		String Northing="";
		String mapKey="";
		Text keyToMap;
		IntWritable one=new IntWritable(1);
		if(DataInLine.length == 8 || DataInLine.length > 7)
		{
		Ctype = DataInLine[7].trim();	
		}
		else
		{
			Ctype= "OTHER";
		}
		try{
				Easting = DataInLine[4].trim();
			    Easting = Easting.substring(0,3);
				Northing = DataInLine[5].trim();
				Northing = Northing.substring(0,3);
				mapKey = Northing + Easting + Ctype;
			keyToMap = new Text(mapKey);
		}
		catch(Exception e){
			mapKey ="OTHERLOCATION"+ Ctype;
			keyToMap = new Text(mapKey);
		}
		Mapcontext.write(keyToMap, one);
}
  
}

}

public static class Reduce extends
	Reducer<Text, IntWritable, Text, IntWritable> {
public void reduce(Text reducerkey, Iterable<IntWritable> inputReducer,
		Context context) throws IOException, InterruptedException {
	int tot=0;
	for(IntWritable input : inputReducer)
	{
		tot = tot + input.get();
	}
	context.write(reducerkey, new IntWritable(tot));
}
}

public static void main(String[] args) throws IOException,
	InterruptedException, ClassNotFoundException {

Job job = new Job();
job.setOutputKeyClass(Text.class);
job.setOutputValueClass(IntWritable.class);
job.setJarByClass(RegionCrimeAnalysis2.class);
job.setMapperClass(CrimeAnalysisDescriptionOne.class);
job.setCombinerClass(Reduce.class);
job.setReducerClass(Reduce.class);

job.setInputFormatClass(TextInputFormat.class);
job.setOutputFormatClass(TextOutputFormat.class);

FileInputFormat.addInputPath(job, new Path(args[0]));
FileOutputFormat.setOutputPath(job, new Path(args[1]));

job.waitForCompletion(true);
}
}
