#
# Copyright (c) 2022 Airbyte, Inc., all rights reserved.
#
import argparse
import os
import shutil
import subprocess
import sys

import boto3


def create_parser():
    parser = argparse.ArgumentParser(description="Package connector for use in snowflake native app")
    parser.add_argument("-module", help="Module to package")
    return parser


def parse_args(args):
    parser = create_parser()
    return parser.parse_args(args)


def get_default_airbyte_path():
    path_to_script = os.path.dirname(__file__)
    relative_path_to_airbyte_root = f"{path_to_script}/../.."
    return os.path.realpath(relative_path_to_airbyte_root)


def make_archive(source, destination):
    print(f"source: {source} destination: {destination}")
    base = os.path.basename(destination)
    name = base.split(".")[0]
    format = "zip"
    archive_from = os.path.dirname(source)
    archive_to = os.path.basename(source.strip(os.sep))
    shutil.make_archive(name, format, archive_from, archive_to)
    shutil.move("%s.%s" % (name, format), destination)


if __name__ == "__main__":
    args = parse_args(sys.argv[1:])
    airbyte_path = get_default_airbyte_path()
    module = args.module
    path_to_module = f"{airbyte_path}/airbyte-integrations/connectors/{module}"
    print(path_to_module)
    current_dir = os.getcwd()
    os.chdir(path_to_module)
    module_root = f"{current_dir}/{module}"
    result = subprocess.run(["pip3", "install", "-r", "requirements.txt", "--target", module_root])
    # subprocess.run(["pip3", "install", "--no-binary=pendulum", "pendulum", "--target", module_root])
    result = subprocess.run(["echo", "hello"])
    os.chdir(current_dir)

    s3 = boto3.resource("s3")
    bucket = "airbyte.alex"

    if result.returncode != 0:
        print(f"Error: {result}")
        exit(result.returncode)
    else:
        print("ok")

        # patch jsonschema https://github.com/python-jsonschema/jsonschema/issues/628
        json_schema_init_filepath = f"{module_root}/jsonschema/__init__.py"
        if os.path.exists(json_schema_init_filepath):
            print("exists!")
            with open(json_schema_init_filepath, "r") as sources:
                lines = sources.readlines()
            with open(json_schema_init_filepath, "w") as sources:
                for line in lines:
                    sources.write(line.replace('metadata.version("jsonschema")', "'3.2.0'"))
        else:
            print("nope")

        # package dependencies
        # for filename in os.listdir(module_root):
        #    path = f"{module_root}/{filename}"
        #    if not path.endswith("dist-info") and not filename.startswith("_"):
        #        # shutil.make_archive(f"{path}", "zip", path)
        #        if os.path.isdir(path) and not path.endswith("dist-info"):
        #            make_archive(path, f"{path}.zip")
        #            object = s3.Bucket(bucket).upload_file(f"{path}.zip", f"{module}/{filename}.zip", ExtraArgs={'ACL': 'public-read'})
        #        else:
        #            object = s3.Bucket(bucket).upload_file(f"{path}", f"{module}/{filename}", ExtraArgs={'ACL': 'public-read'})
        # package connector module
        print(module.replace("-", "_"))
        path_to_connector_module_to_zip = f"{path_to_module}/{module.replace('-', '_')}"
        print(f"path to connector module to zip: {path_to_connector_module_to_zip}")
        # shutil.make_archive(f"{module_root}/{module}", "zip", path_to_connector_module_to_zip)
        make_archive(path_to_connector_module_to_zip, f"{module_root}/{module.replace('-', '_')}.zip")
        object = s3.Bucket(bucket).upload_file(
            f"{module_root}/{module.replace('-', '_')}.zip", f"{module}/{module.replace('-', '_')}.zip", ExtraArgs={"ACL": "public-read"}
        )
