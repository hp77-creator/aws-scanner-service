#!/bin/bash
# Generate and upload 500 small files to S3 for load testing

BUCKET_NAME="aws-scanner-files-649068094730"
PREFIX="load-test"
FILE_COUNT=500

echo "Generating $FILE_COUNT files..."
mkdir -p temp_load_test

for i in $(seq 1 $FILE_COUNT); do
    # Generate a random "SSN" to ensure some files trigger findings
    if (( $i % 10 == 0 )); then
        CONTENT="This file contains a sensitive SSN: $(shuf -i 100-999 -n 1)-$(shuf -i 10-99 -n 1)-$(shuf -i 1000-9999 -n 1)"
    else
        CONTENT="This is safe file number $i"
    fi
    echo "$CONTENT" > "temp_load_test/file_$i.txt"
done

echo "Uploading files to s3://$BUCKET_NAME/$PREFIX/ ..."
aws s3 cp temp_load_test/ "s3://$BUCKET_NAME/$PREFIX/" --recursive --quiet

echo "Done! Uploaded $FILE_COUNT files."
rm -rf temp_load_test
