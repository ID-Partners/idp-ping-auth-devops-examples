#!/bin/bash

BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"
API_KEY="entitlementsRulez"
echo "==== Creating AuthZen TODO Subjects ===="

# Create Manifests for each of the roles

curl -X POST "${BASE_URL}/admin/manifest" \
     -H "${CONTENT_TYPE}" \
     -d '{
  "Name": "Manifest-1",
  "ActionArchetypes": [
    {
      "Name": "viewer",
      "Actions": [
        {
          "Name": "can_read_todo"
        }
      ]
    }
  ],
  "ResourceArchetypes": [
    {
      "Name": "TODO"
    }
  ],
  "SubjectArchetypes": [
    {
      "Name": "viewer"
    }
  ]
}'

curl -X POST "${BASE_URL}/admin/manifest" \
     -H "${CONTENT_TYPE}" \
     -d '{
  "Name": "Manifest-2",
  "ActionArchetypes": [
    {
      "Name": "editor",
      "Actions": [
        {
          "Name": "can_read_todo"
        },
        {
          "Name": "can_create_todo"
        }
      ]
    }
  ],
  "ResourceArchetypes": [
    {
      "Name": "TODO",
      "EligibilityDecisions": [
        {
          "ID": "Eligibility-D1",
          "TTL": "PT24H",
          "rules": ["Direct-Assign"]
        }
      ]
    }
  ],
  "SubjectArchetypes": [
    {
      "Name": "editor"
    }
  ]
}'

curl -X POST "${BASE_URL}/admin/manifest" \
     -H "${CONTENT_TYPE}" \
     -d '{
  "Name": "Manifest-3",
  "ActionArchetypes": [
    {
      "Name": "action-3",
      "Actions": [
        {
          "Name": "can_read_todo"
        },
        {
          "Name": "can_create_todo"
        },
        {
          "Name": "can_delete_todo"
        }
      ]
    }
  ],
  "ResourceArchetypes": [
    {
      "Name": "TODO",
      "EligibilityDecisions": [
        {
          "ID": "Eligibility-D1",
          "TTL": "PT24H",
          "rules": ["Direct-Assign"]
        }
      ]
    }
  ],
  "SubjectArchetypes": [
    {
      "Name": "admin"
    }
  ]
}'


curl -X POST "${BASE_URL}/admin/manifest" \
     -H "${CONTENT_TYPE}" \
     -d '{
  "Name": "Manifest-4",
  "ActionArchetypes": [
    {
      "Name": "action-4",
      "Actions": [
        {
          "Name": "can_read_todo"
        },
        {
          "Name": "can_create_todo"
        },
        {
          "Name": "can_delete_todo"
        },
        {
          "Name": "can_update_todo"
        }
      ]
    }
  ],
  "ResourceArchetypes": [
    {
      "Name": "TODO",
      "EligibilityDecisions": [
        {
          "ID": "Eligibility-D1",
          "TTL": "PT24H",
          "rules": ["Direct-Assign"]
        }
      ]
    }
  ],
  "SubjectArchetypes": [
    {
      "Name": "evil_genius"
    }
  ]
}'

# Rick Sanchez (admin + evil_genius in your original data)
curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "ID": "CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
       "BaseType": 4,
       "ScalarAttributes": {
         "Name": {
           "Name": "Name",
           "Value": "Rick Sanchez",
           "Type": "System.String"
         },
         "Email": {
           "Name": "Email",
           "Value": "rick@the-citadel.com",
           "Type": "System.String"
         }
       },
       "CompoundAttributes": {}
     }'

 curl -X POST "${BASE_URL}/admin/entities/CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{ 
    "sourceID":"CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"evil_genius",
    "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
    "ID": "TODO-1",
    "BaseType": 2
  }'

curl -X POST "${BASE_URL}/admin/entities/TODO-1/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{
  "sourceID":"TODO-1",
  "targetID":"TODO",
  "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities/CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/relationships" \
     -H "Content-Type: application/json" \
     -d '{
    "sourceID":"CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"TODO-1",
    "type":"Owns"
}'

curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
    "ID": "TODO-11",
    "BaseType": 2
  }'

curl -X POST "${BASE_URL}/admin/entities/TODO-11/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{
  "sourceID":"TODO-11",
  "targetID":"TODO",
  "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities/CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/relationships" \
     -H "Content-Type: application/json" \
     -d '{
    "sourceID":"CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"TODO-11",
    "type":"Owns"
}'

curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
    "ID": "TODO-33",
    "BaseType": 2
  }'

curl -X POST "${BASE_URL}/admin/entities/TODO-33/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{
  "sourceID":"TODO-33",
  "targetID":"TODO",
  "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities/CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/relationships" \
     -H "Content-Type: application/json" \
     -d '{
    "sourceID":"CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"TODO-33",
    "type":"Owns"
}'

# Beth Smith (viewer)
curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "ID": "CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
       "BaseType": 4,
       "ScalarAttributes": {
         "Name": {
           "Name": "Name",
           "Value": "Beth Smith",
           "Type": "System.String"
         },
         "Email": {
           "Name": "Email",
           "Value": "beth@the-smiths.com",
           "Type": "System.String"
         }
       },
       "CompoundAttributes": {}
     }'

 curl -X POST "${BASE_URL}/admin/entities/CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{ 
    "sourceID":"CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"viewer",
    "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
    "ID": "TODO-2",
    "BaseType": 2
  }'

curl -X POST "${BASE_URL}/admin/entities/TODO-2/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{
  "sourceID":"TODO-2",
  "targetID":"TODO",
  "type":"IS_A"
  }'


curl -X POST "${BASE_URL}/admin/entities/CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/relationships" \
     -H "Content-Type: application/json" \
     -d '{
    "sourceID":"CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"TODO-2",
    "type":"Owns"
}'


# Morty Smith (editor)
curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "ID": "CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
       "BaseType": 4,
       "ScalarAttributes": {
         "Name": {
           "Name": "Name",
           "Value": "Morty Smith",
           "Type": "System.String"
         },
         "Email": {
           "Name": "Email",
           "Value": "morty@the-citadel.com",
           "Type": "System.String"
         }
       },
       "CompoundAttributes": {}
     }'

 curl -X POST "${BASE_URL}/admin/entities/CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{ 
    "sourceID":"CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"editor",
    "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
    "ID": "TODO-3",
    "BaseType": 2
  }'

curl -X POST "${BASE_URL}/admin/entities/TODO-3/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{
  "sourceID":"TODO-3",
  "targetID":"TODO",
  "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities/CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/relationships" \
     -H "Content-Type: application/json" \
     -d '{
    "sourceID":"CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"TODO-3",
    "type":"Owns"
}'


# Summer Smith (editor)
curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "ID": "CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
       "BaseType": 4,
       "ScalarAttributes": {
         "Name": {
           "Name": "Name",
           "Value": "Summer Smith",
           "Type": "System.String"
         },
         "Email": {
           "Name": "Email",
           "Value": "summer@the-smiths.com",
           "Type": "System.String"
         }
       },
       "CompoundAttributes": {}
     }'

 curl -X POST "${BASE_URL}/admin/entities/CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{ 
    "sourceID":"CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"editor",
    "type":"IS_A"
  }'


curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
    "ID": "TODO-88",
    "BaseType": 2
  }'

curl -X POST "${BASE_URL}/admin/entities/TODO-88/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{
  "sourceID":"TODO-88",
  "targetID":"TODO",
  "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities/CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/relationships" \
     -H "Content-Type: application/json" \
     -d '{
    "sourceID":"CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"TODO-88",
    "type":"Owns"
}'

curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
    "ID": "TODO-89",
    "BaseType": 2
  }'

curl -X POST "${BASE_URL}/admin/entities/TODO-89/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{
  "sourceID":"TODO-89",
  "targetID":"TODO",
  "type":"IS_A"
  }'

curl -X POST "${BASE_URL}/admin/entities/CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/relationships" \
     -H "Content-Type: application/json" \
     -d '{
    "sourceID":"CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"TODO-89",
    "type":"Owns"
}'


# Jerry Smith (viewer)
curl -X POST "${BASE_URL}/admin/entities" \
     -H "${CONTENT_TYPE}" \
     -d '{
       "ID": "CiRmZDQ2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
       "BaseType": 4,
       "ScalarAttributes": {
         "Name": {
           "Name": "Name",
           "Value": "Jerry Smith",
           "Type": "System.String"
         },
         "Email": {
           "Name": "Email",
           "Value": "jerry@the-smiths.com",
           "Type": "System.String"
         }
       },
       "CompoundAttributes": {}
     }'

 curl -X POST "${BASE_URL}/admin/entities/CiRmZDQ2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs/direct-assign-archetype" \
     -H "Content-Type: application/json" \
     -d '{ 
    "sourceID":"CiRmZDQ2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs",
    "targetID":"viewer",
    "type":"IS_A"
  }'


  # Queries 

## Rick 
  curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "SubjectArchetype": "evil_genius",
       "Environments": ["environment1"]
     }'

       curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "SubjectArchetype": "evil_genius",
       "Environments": ["environment1"],
       "Resources": ["TODO-1"]
     }'

  curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Environments": ["environment1"],
       "Actions": ["action-4"],
       "ResourceArchetype": "TODO"
     }'





  curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Environments": ["environment1"],
       "SubjectArchetype": "viewer"
     }'

       curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Resources": ["TODO-1"],
       "Environments": ["environment2"],
        "Actions": ["action1", "action2"]
     }'

     # Summer CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs 

       curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
      "SubjectArchetype": "viewer",
       "Environments": ["environment1"],
       "Actions": ["action-1"]
     }'

       curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDI2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Environments": ["environment1"],
       "Actions": ["action-2"]
     }'




# What resources is Rick a viewer 
  curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
       -H "X-API-Key: $API_KEY" \
     -d '{
       "Subjects": ["CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Actions": ["action1"],
       "Environments": ["environment1"],
       "SubjectArchetype": "viewer"
     }'


    curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Environments": ["environment1"],
       "SubjectArchetype": "evil_genius"
     }'


        curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Actions": ["viewer"],
        "Environments": ["environment1"],
       "ResourceArchetype": "TODO"
     }'


        curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDM2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Resources": ["TODO-2"],
       "Environments": ["environment1"]
     }'


             curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Actions": ["viewer"],
         "Resources": ["TODO-1"],
       "Environments": ["environment1"]
     }'


             curl -X POST "${BASE_URL}/entitlements" \
     -H "Content-Type: application/json" \
     -d '{
       "Subjects": ["CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs"],
       "Actions": ["viewer"],
      "Resources": ["TODO"], 
       "Environments": ["environment1"]
     }'