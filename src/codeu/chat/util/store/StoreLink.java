// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.util.store;


//Susan: I changed this to public to make my life easier
final class StoreLink<KEY, VALUE> {

  public final KEY key;
  public final VALUE value;
  public StoreLink<KEY, VALUE> next;

  public StoreLink(KEY key, VALUE value, StoreLink<KEY, VALUE> next) {
    this.key = key;
    this.value = value;
    this.next = next;
  }
}
