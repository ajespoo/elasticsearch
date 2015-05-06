# TODO

* [X] `SizeHeaderFrameDecoder`: Prevent use of unreleased direct byte bufs by copying every direct byte buf over to heap byte buf and then release the direct byte buf
* [X] Same for HTTP, fix the crude code there and have an own channel handler
* [ ] Potentially use pooled impl for direct byte buffers
* [ ] Debug the configuration on the send side
* [ ] netty 4.1 (check for differences, also ask for release date)

